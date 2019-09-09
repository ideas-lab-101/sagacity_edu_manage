package ltd.linestorm.edu.admin.auth;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/***
 * 集成会话管理
 * 
 * @author Rlax
 * 
 */
public class SessionDAO extends EnterpriseCacheSessionDAO {

	private static final Logger logger = LoggerFactory.getLogger(SessionDAO.class);
	
	public static SessionDAO me;
	private Map<Serializable,Session> map = new HashMap<Serializable,Session>();



	@Override
	public void setCacheManager(CacheManager cacheManager) {
		super.setCacheManager(cacheManager);
		me = this;
	}


	@Override
	protected Serializable doCreate(Session session) {
		Serializable sessionId = super.doCreate(session);
		map.put(sessionId, session);
		return sessionId;
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
//        return super.doReadSession(sessionId);
		return map.get(sessionId);
	}

	@Override
	protected void doUpdate(Session session) {
		super.doUpdate(session);
		map.put(session.getId(),session);
	}

	@Override
	protected void doDelete(Session session) {
		super.doDelete(session);
		map.remove(session.getId());
	}

	/**
	 * 获得所有有效session
	 * @return
	 */
	public Collection<Session> getActiveSessions() {

		return map.values();
	}

	/**
	 * 获得指定session
	 * @param sessionId
	 * @return
	 */
	public Session getActiveSession(Serializable sessionId){
		return  map.get(sessionId);
	}

}
