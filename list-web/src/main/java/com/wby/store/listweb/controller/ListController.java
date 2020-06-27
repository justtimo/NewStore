package com.wby.store.listweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.wby.store.bean.BaseAttrInfo;
import com.wby.store.bean.BaseAttrValue;
import com.wby.store.bean.SkuLsParams;
import com.wby.store.bean.SkuLsResult;
import com.wby.store.service.ListService;
import com.wby.store.service.MangerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    MangerService mangerService;

    @GetMapping("list.html")
    @ResponseBody
    public String list(SkuLsParams skuLsParams, Model model){
        SkuLsResult skuLsResult = listService.getSkuLsInfoList(skuLsParams);
        model.addAttribute("skuLsResult",skuLsResult);

        //得到平台属性列表清单
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = mangerService.getAttrList(attrValueIdList);
        model.addAttribute("attrList",attrList);

        /**
         * 3 点击平台属性值的效果
         *3.1  重新查询商品
         * 			    每个属性值 的点击超链接     当前已有的所有历史条件 + 当前属性值的id
         */
        String paramUrl = makeParamUrl(skuLsParams);
        model.addAttribute("paramUrl",paramUrl);//keyword=小米&valueId=133&valueId=148
        /**
         * 3.2取消点击过的属性值的 对应的属性行
         * 把所有已经选择的数值，从属性+属性值清单中删除属性
         */
        /**
         * 4   面包屑
         *
         *       4.1  点中的属性值 提取到面包屑导航中
         * 	  4.2  当点击面包屑的属性值后 可以取消该属性值的过滤
         * 	      每个面包屑上的url路径 =  历史url参数  - 当前属性值的 valueId
         */
        //已选择的平台属性信息列表
        ArrayList selectedValueList = new ArrayList();


        //清单 attrList
        //一选择的属性值 skuLsParams.getAtrrValueIds
        if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0) {
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {

                    for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                        String selectedValueId = skuLsParams.getValueId()[i];
                        //如果清单中的属性值和已经选择的属性值相同，那么删除对应的属性行
                        if (baseAttrValue.getAttrId().equals(selectedValueId)) {
                            iterator.remove();//删除属性航
                            //2.添加到已选择列表
                            // 4.2 baseAttrValue增加面包屑的url
                            String selectedParamUrl = makeParamUrl(skuLsParams, selectedValueId);
                            baseAttrValue.setParamUrl(selectedParamUrl);

                            selectedValueList.add(baseAttrValue);
                        }
                    }
                }


            }
        }

        model.addAttribute("selectedValueList",selectedValueList);

        model.addAttribute("keyword",skuLsParams.getKeyword());

        /**
         *  5 分页
         *        传递 totalPages  和 pageNo
         */
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        model.addAttribute("totalPages",skuLsResult.getTotalPages());


        //return JSON.toJSONString(skuLsResult);
        return "list";
    }

    /**
     * 把页面传入的参数对象转换为参数url
     * 处理38行的参数
     * @param skuLsParams
     * @return
     */
    public String makeParamUrl(SkuLsParams skuLsParams,String... excludValueId){
        String paramUrl="";
        if (skuLsParams.getKeyword()!=null){
            paramUrl+="keyword"+skuLsParams.getKeyword();
        } else if (skuLsParams.getCatalog3Id()!=null){
            paramUrl+="getCatalog3Id"+skuLsParams.getCatalog3Id();
        }
        if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            for(int i = 0;i<skuLsParams.getValueId().length;i++){
                String valueId=skuLsParams.getValueId()[i];
                //加之前做个判断，用于取消面包屑，排除ValueId
                if (excludValueId!=null&&excludValueId.length>0){
                    String exValueId=excludValueId[0];
                    if (valueId.equals(exValueId)){
                        continue;
                    }

                }

                //加串
                if (paramUrl.length()>0){
                    paramUrl+="&";
                }
                paramUrl+="valueId"+valueId;
            }
        }
        return paramUrl;
    }
}
