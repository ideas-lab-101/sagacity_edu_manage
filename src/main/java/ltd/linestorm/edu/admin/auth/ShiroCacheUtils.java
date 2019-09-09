package ltd.linestorm.edu.admin.auth;

import ltd.linestorm.edu.base.extend.CacheKey;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;

import java.io.Serializable;

/**
 * ShiroCache工具类
 * @author Rlax
 *
 */
public class ShiroCacheUtils
{
	private static CacheManager cacheManager;

	/**
	 * 清除用户的授权信息
	 * 
	 * @param username
	 */
	public static void clearAuthorizationInfo(String username)
	{
		Cache<Object, Object> cache = cacheManager.getCache(CacheKey.CACHE_SHIRO_AUTHORIZATION);
		if(cache != null){
			cache.remove(username);
		}
	}

	public static void clearAuthorizationInfoAll()
	{
		Cache<Object, Object> cache = cacheManager.getCache(CacheKey.CACHE_SHIRO_AUTHORIZATION);
		if(cache != null){
			cache.clear();
		}
	}

	public static void clearSessionAll(){
        Cache<Object, Object> cache = cacheManager.getCache(CacheKey.CACHE_SHIRO_ACTIVESESSION);
        if(cache != null){
            cache.clear();
        }
    }

	public static CacheManager getCacheManager()
	{
		return cacheManager;
	}

	public static void setCacheManager(CacheManager cacheManager)
	{
		ShiroCacheUtils.cacheManager = cacheManager;
	}

}
