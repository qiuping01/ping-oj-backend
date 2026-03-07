package com.ping.ojcodesandbox.security;

import java.security.Permission;

/**
 * 我的安全管理器
 */
public class MySecurityManager extends SecurityManager {

    /**
     * 检查所有权限
     *
     * @param perm the requested permission.
     */
    @Override
    public void checkPermission(Permission perm) {
//        super.checkPermission(perm);
    }

    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("checkExec 权限异常:" + cmd);
    }

    @Override
    public void checkRead(String file) {
//        if (file.contains("hutool")){
//            return;
//        }
//         throw new SecurityException("checkRead 权限异常:" + file);
    }

    @Override
    public void checkWrite(String file) {
//        throw new SecurityException("checkWrite 权限异常:" + file);
    }

    @Override
    public void checkDelete(String file) {
//        throw new SecurityException("checkDelete 权限异常:" + file);
    }

    @Override
    public void checkConnect(String host, int port) {
//        throw new SecurityException("checkConnect 权限异常:" + host + port);
    }
}
