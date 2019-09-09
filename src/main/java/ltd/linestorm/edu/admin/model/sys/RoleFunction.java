package ltd.linestorm.edu.admin.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="sys_role_func",pkName="mapping_id")
public class RoleFunction extends Model<RoleFunction> {
	public final static RoleFunction dao = new RoleFunction();

}
