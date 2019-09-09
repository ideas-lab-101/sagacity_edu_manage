package ltd.linestorm.edu.base.extend;

/**
 * 常量
 * @author Rlax
 *
 */
public class Consts
{
	/** session 用户信息key */
	public static final String SESSION_USER = "user";
	/** session 用户角色key */
	public static final String SESSION_USER_ROLE = "userRole";
	public static final String SESSION_EAMIL_USER="email_val";
	public static final String SESSION_MESSAGE = "_message";
	/** 短信session值 */
	public static final String SESSION_SMS = "_sms_val";
	
	/** 微信授权 token 存储在 session 里 对应的 key */
	public static final String WEIXIN_SESSION_CODE = "weixin_code";
	
	public static final int PAGE_DEFAULT_SIZE = 10;
	
	/** 定位当前菜单功能标识 */
	public static final String FUNC_TAG = "_func";

	/** 表单token 防止重复提交 */
	public static final String TOKEN_TAG = "_token";

	/** 默认的机构和平台管理员ID */
	public final static String DEFAULT_ORGID = "888888";

	public final static String DEFAULT_ADMINID = "666666";

	/**
	 * 资源类型
	 */
	public final static String FUNC_MODULE = "module";
	public final static String FUNC_MENU = "menu";
	public final static String FUNC_Button = "button";
	
	public final static String ORDER_UP = "up";
	public final static String ORDER_DOWN = "down";
}
