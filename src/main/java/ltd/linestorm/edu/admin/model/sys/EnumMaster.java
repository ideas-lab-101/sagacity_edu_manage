package ltd.linestorm.edu.admin.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="sys_enum_master",pkName="master_id")
public class EnumMaster extends Model<EnumMaster> {
	public final static EnumMaster dao = new EnumMaster();

	public boolean checkData(int master_id){
		if(Db.findFirst("select * from sys_enum_detail where master_id=?", master_id) != null){
			return true;
		}
		return  false;
	}
}
