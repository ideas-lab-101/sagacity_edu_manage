package ltd.linestorm.edu.admin.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="sys_function",pkName="func_id")
public class Function extends Model<Function> {
	public final static Function dao = new Function();

}
