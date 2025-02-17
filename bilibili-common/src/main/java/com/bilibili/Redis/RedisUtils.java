package com.bilibili.Redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis的常用操作
 * @param <V>
 */
@Component
public class RedisUtils<V> {
    @Autowired
    private RedisTemplate<String,V> redisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 删除一个或者多个key
     * @param key
     */
    public void delete(String...key){
        if(key != null && key.length>0){
            if(key.length == 1){
                redisTemplate.delete(key[0]);
            }else{
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     * 查询一个
     * @param key
     * @return
     */
    public V get(String key){
        return key == null?null:redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key,V value){
        try {
            redisTemplate.opsForValue().set(key,value);
            return true;
        } catch (Exception e) {
            LOGGER.error("普通缓存放入失败");
            return false;
        }
    }

    /**
     * 检查是否存在值
     * @param key
     * @return
     */
    public boolean existsValue(String key){
        V value = null;
        try {
            value = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            LOGGER.error("检查是否存在出错");
        }
        if(value == null){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 检查是否存在key
     * @param key
     * @return
     */
    public boolean existsKey(String key){
        Boolean flag = false;
        flag = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(flag);
    }

    /**
     * 设置过期时间
     * @param key
     * @param value
     * @param time
     * @return
     */
    public boolean setex(String key,V value,long time){
        try {
            if(time > 0){
                redisTemplate.opsForValue().set(key,value,time, TimeUnit.MILLISECONDS);
            }else{
                redisTemplate.opsForValue().set(key,value);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("设置过期时间key出错");
            return false;
        }
    }

    /**
     * 设置过期时间
     * @param key
     * @param time
     * @return
     */
    public boolean expire(String key,long time){
        try {
            if(time > 0){
                redisTemplate.expire(key,time,TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("设置过期时间出错");
            return false;
        }
    }

    /**
     * 获取list的所有元素
     * @param key
     * @return
     */
    public List<V> getQueueList(String key){
        return redisTemplate.opsForList().range(key,0,-1);
    }

    /**
     * 向list左侧插入数据以及时间
     * @param key
     * @param value
     * @param time
     * @return
     */
    public boolean lpush(String key,V value,Long time){
        try {
            redisTemplate.opsForList().leftPush(key,value);
            if(time > 0){
                redisTemplate.expire(key,time,TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("list左插入失败");
            return false;
        }
    }

    /**
     * 从list中删除元素
     * @param key
     * @param value
     * @return
     */
    public long remove(String key,V value){
        try {
            Long remove = redisTemplate.opsForList().remove(key,1,value);
            return remove;
        } catch (Exception e) {
            LOGGER.error("删除list中元素出错");
            return 0;
        }
    }

    /**
     * list左侧插入集合元素
     * @param key
     * @param list
     * @param time
     * @return
     */
    public boolean lpushAll(String key,List<V> list,Long time){
        try {
            redisTemplate.opsForList().leftPushAll(key,list);
            if(time>0){
                expire(key,time);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("list左侧插入列表失败");
            return false;
        }
    }

    /**
     * list右侧弹出元素
     * @param key
     * @return
     */
    public V rpop(String key){
        try {
            V v = null;
            v = redisTemplate.opsForList().rightPop(key);
            return v;
        } catch (Exception e) {
            LOGGER.error("list右侧弹出失败");
            return null;
        }
    }

    /**
     * key对应的value递增
     * @param key
     * @return
     */
    public Long increment(String key){
        Long count = redisTemplate.opsForValue().increment(key);
        return count;
    }

    /**
     * 如果是第一次被递增，则设置过期时间
     * @param key
     * @param milliseconds
     * @return
     */
    public Long incrementex(String key,long milliseconds){
        Long count = redisTemplate.opsForValue().increment(key,1);
        if(count == 1){
            expire(key,milliseconds);
        }
        return count;
    }

    /**
     * key递减
     * @param key
     * @return
     */
    public Long decrement(String key){
        Long count = redisTemplate.opsForValue().increment(key,-1);
        if(count < 0){
            redisTemplate.delete(key);
        }
        return count;
    }

    /**
     * 根据前缀得到所有value
     * @param prefix
     * @return
     */
    public Set<String> gerKeysByPrefix(String prefix){
        String key = prefix+"*";
        return redisTemplate.keys(key);
    }

    /**
     * 根据前缀获取所有键值对
     * @param prefix
     * @return
     */
    public Map<String,V> getBatch(String prefix){
        Set<String> keys = gerKeysByPrefix(prefix);
        List<String> list = new ArrayList<>(keys);
        List<V> values = redisTemplate.opsForValue().multiGet(list);
        Map<String,V> map = list.stream().collect(Collectors.toMap(key->key,value->values.get(list.indexOf(value))));
        return map;
    }

    /**
     * 给redis中zset中的指定元素加1
     * @param key
     * @param v
     */
    public void zaddCount(String key,V v){
        redisTemplate.opsForZSet().incrementScore(key,v,1);
    }

    /**
     * 获取redis中zset的前count个元素
     * @param key
     * @param count
     * @return
     */
    public List<V> getZSetList(String key,Integer count){
        Set<V> gets = redisTemplate.opsForZSet().range(key,0,count);
        List<V> res = new ArrayList<>(gets);
        return res;
    }
}
