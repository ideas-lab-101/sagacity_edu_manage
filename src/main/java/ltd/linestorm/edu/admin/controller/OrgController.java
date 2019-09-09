package ltd.linestorm.edu.admin.controller;

import com.jfinal.ext.route.ControllerBind;
import ltd.linestorm.edu.admin.common.AdminBaseController;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

@ControllerBind(controllerKey = "/org",viewPath = "/static/org")
@RequiresAuthentication
public class OrgController extends AdminBaseController {

    @Override
    public void index(){
        render("orgManage.html");
    }
}
