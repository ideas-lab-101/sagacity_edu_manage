package ltd.linestorm.edu.service.openapi;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.PropertiesFactoryHelper;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Qiniu {

    public final static Qiniu dao = new Qiniu();

    public String getUploadToken(){
        //获取7牛的参数
        String accessKey = PropertiesFactoryHelper.getInstance()
                .getConfig("qiniu.accessKey");
        String secretKey = PropertiesFactoryHelper.getInstance()
                .getConfig("qiniu.secretKey");
        String bucket = PropertiesFactoryHelper.getInstance()
                .getConfig("qiniu.bucket");

        Auth auth = Auth.create(accessKey, secretKey);
        //自定义返回参数
        //StringMap putPolicy = new StringMap();
        //putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"fsize\":$(fsize)}");

        return auth.uploadToken(bucket);
    }

    public boolean removeFile(String hash){
        boolean r = false;
        Configuration cfg = new Configuration(Zone.zone2());
        String accessKey = PropertiesFactoryHelper.getInstance()
                .getConfig("qiniu.accessKey");
        String secretKey = PropertiesFactoryHelper.getInstance()
                .getConfig("qiniu.secretKey");
        String bucket = PropertiesFactoryHelper.getInstance()
                .getConfig("qiniu.bucket");

        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(bucket, hash);
            r = true;
        } catch (QiniuException ex) {
            //如果遇到异常，说明删除失败
            System.err.println(ex.code());
            System.err.println(ex.response.toString());
            r = false;
        }
        return  r;
    }

    /**
     * 针对私密空间
     * @param url
     * @return
     */
    public String download(String url){
        String accessKey = PropertiesFactoryHelper.getInstance()
                .getConfig("qiniu.accessKey");
        String secretKey = PropertiesFactoryHelper.getInstance()
                .getConfig("qiniu.secretKey");
        Auth auth = Auth.create(accessKey, secretKey);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String finalUrl = auth.privateDownloadUrl(url, expireInSeconds);
        return finalUrl;
    }

    public JSONObject uploadFile(File nFile, String upToken, String prefix){
        String fileName = nFile.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String key = prefix+ DateUtils.getLongDateMilliSecond()+"."+suffix;
        Configuration cfg = new Configuration(Zone.zone2()); //华南
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            InputStream input = new FileInputStream(nFile);
            byte[] byt = new byte[input.available()];
            input.read(byt);
            try {
                Response response = uploadManager.put(byt, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                JSONObject info = new JSONObject();
                info.put("key", putRet.key);
                info.put("hash", putRet.hash);
                return info;
//                System.out.println(putRet.key);
//                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
                return null;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static void main (String[]args){
        String key = "http://cloud-course.ideas-lab.cn/record/o0aIu5VTV6i7ckVG1ipoOiXuLPxc/20180911183714391.mp3";
        String key1 = "http://cloud-course.ideas-lab.cn/";
        Qiniu.dao.removeFile(key.replace(key1,""));
    }
}
