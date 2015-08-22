package com.codeyn.base.upyun;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class UpYunUtils {
    /**
     * saveKey表示在服务器上存储文件的位置和文件名，如果服务器上已有同名文件，将会默认直接覆盖原有文件。 通常使用
     * “/filepath/filename” 的形式来保存文件。 expiration表示过期时间，使用的是unix time bucket
     * 表示云存储的空间名
     * 
     * @param saveKey
     * @param expiration
     * @param bucket
     * @return
     */
    public static String makePolicy(String saveKey, long expiration, String bucket) throws UpYunException {

        return makePolicy(saveKey, expiration, bucket, null);
    }

    /**
     * saveKey表示在服务器上存储文件的位置和文件名，如果服务器上已有同名文件，将会默认直接覆盖原有文件。 通常使用
     * “/filepath/filename” 的形式来保存文件。 expiration表示过期时间，使用的是unix time bucket
     * 表示云存储的空间名 params 其他参数请参考又拍云的api文档
     * 
     * @param saveKey
     * @param expiration
     * @param bucket
     * @param params
     * @return
     * @throws UpYunException
     */
    public static String makePolicy(String saveKey, long expiration, String bucket, Map<String, Object> params)
            throws UpYunException {

        if (saveKey == null || saveKey.equals("")) {
            throw new UpYunException(20, "miss param saveKey");
        }
        if (expiration == 0) {
            throw new UpYunException(20, "miss param expiration");
        }
        if (bucket == null || bucket.equals("")) {
            throw new UpYunException(20, "miss param bucket");
        }

        JSONObject obj = new JSONObject();
        try {
            obj.put("save-key", saveKey);
            obj.put("expiration", expiration);
            obj.put("bucket", bucket);

            if (params != null) {
                Set<String> keys = params.keySet();
                for (String key : keys) {
                    obj.put(key, params.get(key));
                }
            }

        } catch (JSONException e) {
            throw new UpYunException(21, e.getMessage());
        }

        return Base64.encodeBase64String(obj.toString().getBytes(Charsets.UTF_8));
    }

    /**
     * source String = "policy + & + 表单API验证密钥" 获得签名数据
     * <b>通常我们建议签名数据在服务器端生成，仅在手机端需要上传文件的时候，才从服务器端取得签名后的数据，以防止表单API验证密钥泄露出去。</b>
     * 
     * @param source
     * @return
     */
    public static String signature(String source) {

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.reset();
            md.update(source.getBytes());

            byte[] mdbytes = md.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                String hex = Integer.toHexString(0xff & mdbytes[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}