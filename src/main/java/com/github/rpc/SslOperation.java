package com.github.rpc;

import java.io.File;

/**
 * @author Ray
 * @date created in 2022/3/7 9:06
 */
public interface SslOperation {
    void enableSsl(File jksFile, String password, boolean needClientAuth);
}
