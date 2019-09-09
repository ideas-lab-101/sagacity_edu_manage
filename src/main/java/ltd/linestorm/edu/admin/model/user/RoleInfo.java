package ltd.linestorm.edu.admin.model.user;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;
import ltd.linestorm.edu.admin.model.sys.Function;

import java.util.List;

@TableBind(tableName="sys_roles",pkName="role_id")
public class RoleInfo extends Model<RoleInfo> {
	public final static RoleInfo dao = new RoleInfo();


	public List<Function> getResources(int roleId, String type){
	    String sql = "select mf.*\n" +
                "from sys_function mf\n" +
                "inner join sys_role_func rf on rf.func_id = mf.func_id\n" +
                "where rf.role_id=? and mf.state=1\n";
        if(StrKit.notNull(type)){
            sql += " and mf.func_type='"+type+"'";
        }
        sql += " order by mf.order";
		List<Function> funcList = Function.dao.find(sql, roleId);
		return funcList;
	}

}
