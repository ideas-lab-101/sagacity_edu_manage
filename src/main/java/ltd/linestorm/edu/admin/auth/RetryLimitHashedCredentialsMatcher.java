package ltd.linestorm.edu.admin.auth;

import com.jfinal.plugin.ehcache.CacheKit;
import ltd.linestorm.edu.base.extend.CacheKey;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 密码重试认证
 * @author Rlax
 *
 */
public class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher {

	/** 允许密码重试最大次数 */
	private int allowRetryCount = 5;

	@Override
	public boolean doCredentialsMatch(AuthenticationToken _token, AuthenticationInfo info) {
		UsernamePasswordToken token = (UsernamePasswordToken) _token;

		String accountName = (String) token.getPrincipal();
		AtomicInteger atomicInteger = CacheKit.get(CacheKey.CACHE_SHIRO_PASSWORDRETRY, accountName);

		if (atomicInteger == null) {
			atomicInteger = new AtomicInteger(1);
		} else {
			atomicInteger.incrementAndGet();
		}
		CacheKit.put(CacheKey.CACHE_SHIRO_PASSWORDRETRY, accountName, atomicInteger);

		if (atomicInteger.get() > allowRetryCount) {
			throw new ExcessiveAttemptsException();
		}
		boolean matches = super.doCredentialsMatch(token, info);
		//验证成功，则尝试次数清空
		if (matches) {
            CacheKit.remove(CacheKey.CACHE_SHIRO_PASSWORDRETRY, accountName);
		}
		
		return matches;
	}

	public int getAllowRetryCount() {
		return allowRetryCount;
	}

	public void setAllowRetryCount(int allowRetryCount) {
		this.allowRetryCount = allowRetryCount;
	}
}
