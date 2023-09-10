package com.atguigu.gmall.common.fastdfs;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * demo
 */
@Service
public class Test {

    static {
        try {
            //初始化连接
            ClassPathResource resource = new ClassPathResource("tracker.conf");
            ClientGlobal.init(resource.getPath());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * @param file
     * @return
     */
    public String upload(MultipartFile file) throws Exception{
        //获取tracker
        TrackerClient trackerClient = new TrackerClient();
        //获取连接
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取storage
        StorageClient storageClient = new StorageClient(trackerServer, null);
        //执行文件上传
        /**
         * 1.文件的字节码
         * 2.文件的拓展名
         * 3.附加参数: 时间 地点 设备....
         */
        String[] strings = storageClient.upload_file(file.getBytes(),
                StringUtils.getFilenameExtension(file.getOriginalFilename()),
                null);

        return strings[0] + "/" + strings[1];
    }

    /**
     * 文件下载
     * @param groupName
     * @param path
     * @throws Exception
     */
    public byte[] download(String groupName,String path) throws Exception{
        //获取tracker
        TrackerClient trackerClient = new TrackerClient();
        //获取连接
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取storage
        StorageClient storageClient = new StorageClient(trackerServer, null);
        //下载
        byte[] bytes = storageClient.download_file(groupName, path);
        return bytes;
    }

    /**
     * 文件删除
     * @param groupName
     * @param path
     * @return
     * @throws Exception
     */
    public boolean del(String groupName,String path) throws Exception{
        //获取tracker
        TrackerClient trackerClient = new TrackerClient();
        //获取连接
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取storage
        StorageClient storageClient = new StorageClient(trackerServer, null);
        //文件删除
        int i = storageClient.delete_file(groupName, path);
        //返回结果
        return i >=0?true:false;
    }
}
