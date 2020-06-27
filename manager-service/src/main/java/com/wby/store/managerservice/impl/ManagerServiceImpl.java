package com.wby.store.managerservice.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.wby.store.bean.*;
import com.wby.store.managerservice.mapper.*;
import com.wby.store.service.MangerService;
import com.wby.store.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.security.auth.login.AccountLockedException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ManagerServiceImpl implements MangerService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    public static final String  SKUKEY_PREFIX="sku:";
    public static final String  SKUKEY_INFO_SUFFIX=":info";
    public static final String  SKUKEY_LOCK_SUFFIX=":lock";
    //public static final String  int SKU_EXPRIRE=3;先不写在这里，写在方法里方便调试

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        Example example=new Example(BaseCatalog2.class);
        //andEqualTo()第一个参数是实体类中的属性(property)，而不是数据库中的字段名,如果是数据库中的话应该是
        //column或者filed
        example.createCriteria().andEqualTo("catalog1Id",catalog1Id);
        return baseCatalog2Mapper.selectByExample(example);
       /* BaseCatalog2 baseCatalog2=new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> select = baseCatalog2Mapper.select(baseCatalog2);
        return select;*/
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        Example example=new Example(BaseCatalog3.class);
        example.createCriteria().andEqualTo("catalog2Id",catalog2Id);
        return baseCatalog3Mapper.selectByExample(example);
        /*BaseCatalog3 baseCatalog3=new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> select = baseCatalog3Mapper.select(baseCatalog3);
        return select;*/
    }


    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        //使用通用mapper在78行会循环连接数据库查询，数据多的时候性能压力大，现在换成mybatis的xml方式自定义查询
        /*Example example=new Example(BaseAttrInfo.class);
        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);

        List<BaseAttrInfo> baseAttrInfoList=baseAttrInfoMapper.selectByExample(example);
        //查询平台属性之
        for (BaseAttrInfo baseAttrInfo:baseAttrInfoList
             ) {
            BaseAttrValue baseAttrValue=new BaseAttrValue();
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
            baseAttrInfo.setAttrValueList(baseAttrValueList);
        }

        //return baseAttrInfoMapper.selectByExample(example);
        return baseAttrInfoList;*/

        //使用mybatis自定义关联查询。因为通用mapper只能进行单表查询，无法多表关联查询
        List<BaseAttrInfo> baseAttrInfoListByCatalog3Id =
                baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
        return baseAttrInfoListByCatalog3Id;
    }

    @Override
    @Transactional  //涉及多表，添加事务
    public void saveBaseAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId()!=null&&baseAttrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else {
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //根据attrId先全部删除属性值，在统一保存
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId",baseAttrInfo.getId());
        baseAttrValueMapper.deleteByExample(example);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            String id = baseAttrInfo.getId();
            baseAttrValue.setAttrId(id);
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        Example example=new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId",attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectByExample(example);
        baseAttrInfo.setAttrValueList(baseAttrValueList);
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        Example example=new Example(SpuInfo.class);
        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
        return spuInfoMapper.selectByExample(example);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //spu基本信息
        if (spuInfo.getId()==null || spuInfo.getId().length()==0){
            //保存
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }else {
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }
        //spuImage图片列表，先删除吗，再新增

        spuImageMapper.deleteByPrimaryKey(spuInfo.getId());
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage image:spuImageList
             ) {
            image.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(image);
        }
        //销售属性。删除、新增
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);

        // 销售属性值 删除，插入
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        // 获取数据
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList!=null && spuSaleAttrList.size()>0){
            // 循环遍历
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                saleAttr.setId(null);
                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(saleAttr);

                // 添加销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                    // 循环遍历
                    for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                        saleAttrValue.setId(null);
                        saleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(saleAttrValue);
                    }
                }
            }
            }
        }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage=new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListBySpuId(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        //1.保存基本信息
        if (skuInfo.getId()==null||skuInfo.getId().length()==0){
            skuInfoMapper.insertSelective(skuInfo);
        }else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //2.保存平台属性
        SkuAttrValue skuAttrValue=new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue1 :skuAttrValueList
                ) {
            skuAttrValue1.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(skuAttrValue1);
        }
        //3.保存销售属性
        SkuSaleAttrValue skuSaleAttrValue=new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue saleAttrValue:skuSaleAttrValueList
             ) {
            saleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }
        //4.保存图片
        SkuImage skuImage=new SkuImage();
        skuImage.setId(skuInfo.getId());
        skuImageMapper.delete(skuImage);
        for (SkuImage skuImage1: skuInfo.getSkuImageList()
             ) {
            skuImage1.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(skuImage1);
        }
    }

    @Override   //不带缓存
    public SkuInfo getSkuINfoDB(String skuId) {
        System.out.println(Thread.currentThread()+"正在根据skuId查询数据库数据");
        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        /*Example example = new Example(SkuInfo.class);
        example.createCriteria().andEqualTo("id",skuId);*/
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if (skuInfo==null){
            return null;
        }

        //查询图片
        SkuImage skuImage=new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImages);

        //查询sku销售属性值
        SkuSaleAttrValue skuSaleAttrValue=new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList =
                skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        //查询平台属性
        SkuAttrValue skuAttrValue=new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList =
                skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return skuInfo;

    }
   //带缓存
   @Override
    public SkuInfo getSkuINfo_reids(String skuId) {
            SkuInfo skuInfoResult=null;
            //1.先查redis，么有再查数据库
            Jedis jedis=redisUtil.getJedis();

            int SKU_EXPIRE_SEC=3*60*60*12;
        /**
         * 首先要考虑redis结构：type（string,set,list,hash,zset）
         *
         * key  sku:id:info 比如sku:101:海报  sku:101:详情  sku:101:点赞数
         *      为什么不用hash？因为没有独立的过期时间，就像101的详情过期1天、海报过期7天
         * value
         */
        String skuKey=SKUKEY_PREFIX+skuId+SKUKEY_INFO_SUFFIX;
        String skuInfoJson = jedis.get(skuKey);
        if (skuInfoJson!=null){
            if(!"EMPTY".equals(skuInfoJson)){
                System.err.println(Thread.currentThread()+"命中缓存了！使用redis");
                skuInfoResult=JSON.parseObject(skuInfoJson,SkuInfo.class);
            }

        }else {
            System.out.println(Thread.currentThread()+"没有命中缓存了！查询数据库");
            //查锁解锁
            //定义一下锁结构。用Redis无脑三件套：type（String）、key（sku:101:lock）、value(locked或者1、0)
            String lockKey=SKUKEY_PREFIX+skuId+SKUKEY_LOCK_SUFFIX;
            /*Long locked = jedis.setnx(lockKey, "locked");
            jedis.expire(lockKey,10);//设置过期时间，但是设置锁和设置过期时间分成两步还是会有破绽。
            使用带过期时间的set方法
            */
            //set(String key,String value,String nxxx,String expx,time)，返回OK/null
            //nxxx:nx(没有的时候设置值，xx有则设值)
            //expx:ex(以秒为单位，px以毫秒为单位)
            String token= UUID.randomUUID().toString();
            String locked = jedis.set(lockKey, token, "NX", "EX", 10);
            if ("OK".equals(locked)){//值为1表示redis成功上锁了。
                System.out.println(Thread.currentThread()+"得到锁了");
                skuInfoResult=getSkuINfoDB(skuId);
                //解决缓存穿透,当查询数据库的skuINfoJSonResult为null时，就给他设置一个值
                System.out.println(Thread.currentThread()+"数据库查询完毕，开始写入缓存");
                String skuINfoJSonResult =null;
               if(skuInfoResult!=null){
                   skuINfoJSonResult = JSON.toJSONString(skuInfoResult);
               }else{
                   skuINfoJSonResult="EMPTY";
               }
                jedis.setex(skuKey,SKU_EXPIRE_SEC,skuINfoJSonResult);//写入数据，并设置过期时间
                System.out.println(Thread.currentThread()+"开始释放锁"+lockKey);
                if(jedis.exists(lockKey)&&token.equals(jedis.get(lockKey))){
                    jedis.del(lockKey);//存在问题，当这个if判断成功的瞬间，锁失效另外线程进来了，那么
                    //删除的是另外一个现成的锁。也就是这里并不是原子性的
                }

            }else{
                System.out.println(Thread.currentThread()+"没有得到锁，轮训自旋中");
                //怎么知道用完了？轮训自选等待。等待1秒，然后再调用自己这个方法，看是否命中缓存
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getSkuINfo( skuId);
            }

           }
        /**
         * 上面这种方式有个问题，当连续点击浏览器的刷新时候，会出现与代码逻辑不符的情况。
         * 这就是缓存击穿（需要注意和缓存穿透的区别。
         * 缓存穿透：
         *      用户想要查询一个数据，发现redis内存数据库没有，也就是缓存没有命中，
         *      于是向持久层数据库查询。发现也没有，于是本次查询失败。当用户很多的时候，
         *      缓存都没有命中，于是都去请求了持久层数据库。这会给持久层数据库造成很大的压力，
         *      这时候就相当于出现了缓存穿透。
         *
         * 缓存击穿：
         *      一个key非常热点，在不停的扛着大并发，大并发集中对这一个点进行访问，当这个key在失效的瞬间，
         *      持续的大并发就穿破缓存，直接请求数据库，就像在一个屏障上凿开了一个洞。
         *
         * 缓存击穿解决方案：
         *      1.redis高可用，也就是redis集群
         *      2.限流降级。缓存失效后，通过加锁或者队列来控制读数据库写缓存的线程数量。比如对某个key只允许一个线程查询数据和写缓存，其他线程等待。
         *      3.数据预热：在正式部署之前，我先把可能的数据先预先访问一遍，这样部分可能大量访问的数据就会加载到缓存中。在即将发生大并发访问前手动触发加载缓存不同的key，
         *          设置不同的过期时间，让缓存失效的时间点尽量均匀。
         *
         * 使用传统的lock、synchronized ()有个限制，只能有一台机器。虽然当前机器只有一个线程，但是
         * 底层的数据库，如果有10台tomcat，那么访问数据库的就还是会有10个，不是1个
         *
         * 分布式锁：用redis，因为redis本身是单线程的，不存在并发
         * jedis.setnx()：查锁+抢锁。为什么不用查锁exists+抢锁set两个方法？
         *      有风险，查锁的间隙另外一个吧锁抢走了。用一个方法就不会有人插队，源于redis的单线程和原子性
         */



        jedis.close();
        return skuInfoResult;
    }

    //使用redisson
    @Override
    public SkuInfo getSkuINfo(String skuId) {
        SkuInfo skuInfoResult=null;
        //1.先查redis，么有再查数据库
        Jedis jedis=redisUtil.getJedis();
        int SKU_EXPIRE_SEC=3*60*60*12;
        /**
         * 首先要考虑redis结构：type（string,set,list,hash,zset）
         *
         * key  sku:id:info 比如sku:101:海报  sku:101:详情  sku:101:点赞数
         *      为什么不用hash？因为没有独立的过期时间，就像101的详情过期1天、海报过期7天
         * value
         */
        String skuKey=SKUKEY_PREFIX+skuId+SKUKEY_INFO_SUFFIX;
        String skuInfoJson = jedis.get(skuKey);
        if (skuInfoJson!=null){
            if(!"EMPTY".equals(skuInfoJson)){
                System.err.println(Thread.currentThread()+"命中缓存了！使用redis");
                skuInfoResult=JSON.parseObject(skuInfoJson,SkuInfo.class);
            }

        }else {
            Config config=new Config();
            config.useSingleServer().setAddress("redis://47.97.125.139:6379");

            RedissonClient redissonClient= Redisson.create(config);
            String lockKey=SKUKEY_PREFIX+skuId+SKUKEY_LOCK_SUFFIX;
            RLock lock = redissonClient.getLock(lockKey);
            //lock.lock(10, TimeUnit.SECONDS);
            boolean locked=false;
            try {
                //l:等待时间    l1：上锁时间
                locked = lock.tryLock(10, 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //得到锁，处理业务
            if (locked){
                System.err.println(Thread.currentThread()+"得到锁");

            //如果得到锁后能够在缓存中查询，那么久直接使用缓存，不再查询数据库
                System.err.println(Thread.currentThread()+"查询缓存");
            skuInfoJson = jedis.get(skuKey);
            if (skuInfoJson!=null){
                if(!"EMPTY".equals(skuInfoJson)){
                    System.err.println(Thread.currentThread()+"命中缓存了！使用redis");
                    skuInfoResult=JSON.parseObject(skuInfoJson,SkuInfo.class);
                }

            }else{
                System.err.println(Thread.currentThread()+"缓存中没有查到，开始查询数据库");
                skuInfoResult=getSkuINfoDB(skuId);
                System.out.println(Thread.currentThread()+"数据库查询完毕，开始写入缓存");
                String skuINfoJSonResult =null;
                if(skuInfoResult!=null){
                    skuINfoJSonResult = JSON.toJSONString(skuInfoResult);
                }else{
                    skuINfoJSonResult="EMPTY";
                }
                jedis.setex(skuKey,SKU_EXPIRE_SEC,skuINfoJSonResult);//写入数据，并设置过期时间

            }
            //释放锁
            lock.unlock();
            }

        }
        jedis.close();
        return skuInfoResult;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckSku(String skuId, String spuId) {
        List<SpuSaleAttr> spuSaleAttrList =
                spuSaleAttrMapper.getSpuSaleAttrListBySpuIdCheckSku(skuId,spuId);
        return  spuSaleAttrList;
    }

    @Override
    public Map getSkuValueIdsMap(String spuId) {
        List<Map> saleAttrValuesBySpu =
                skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);
        Map skuValueIds=new HashMap();
        for (Map map:saleAttrValuesBySpu
             ) {
            String  skuId=(Long)  map.get("sku_id")+"";

            String  valueIds=(String)  map.get("value_ids");
            skuValueIds.put(valueIds,skuId);
        }
        return skuValueIds;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List attrValueIdList) {
        //List转换为13,14,15.
        String valueIds = StringUtils.join(attrValueIdList, ",");
        List<BaseAttrInfo> baseAttrInfoListByValueIds = baseAttrInfoMapper.getBaseAttrInfoListByValueIds(valueIds);
        return baseAttrInfoListByValueIds;
    }


}
