package ltd.linestorm.edu.admin.model.user;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="user_profile",pkName="profile_id")
public class UserProfile extends Model<UserProfile> {
	public final static UserProfile dao = new UserProfile();

}
