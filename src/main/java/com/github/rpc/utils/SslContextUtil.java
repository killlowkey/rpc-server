package com.github.rpc.utils;

import org.tinylog.Logger;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * @author Ray
 * @date created in 2022/3/7 10:25
 */
public class SslContextUtil {

    public static SSLContext getSslContext(File jksFile, String keyStorePass) {
        try {
            SSLContext context = SSLContext.getInstance("SSLv3");
            KeyManager[] keyManagers = getKeyManagers(jksFile, keyStorePass);
            TrustManager[] trustManagers = getTrustManagers(jksFile, keyStorePass);
            if (keyManagers != null && trustManagers != null) {
                context.init(keyManagers, trustManagers, null);
            }
            context.createSSLEngine().getSupportedCipherSuites();
            return context;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static KeyManager[] getKeyManagers(File jksFile, String keyStorePass) throws Exception{
        try (FileInputStream is = new FileInputStream(jksFile)) {
            KeyStore ks = KeyStore.getInstance("JKS");
            KeyManagerFactory keyFac = KeyManagerFactory.getInstance("SunX509");
            ks.load(is, keyStorePass.toCharArray());
            keyFac.init(ks, keyStorePass.toCharArray());
            return keyFac.getKeyManagers();
        } catch (Exception ex) {
            Logger.error("create KeyManager failed, ex：{}", ex.getMessage());
            throw ex;
        }
    }


    private static TrustManager[] getTrustManagers(File jksFile, String keyStorePass) throws Exception {
        try (FileInputStream is = new FileInputStream(jksFile)) {
            KeyStore ks = KeyStore.getInstance("JKS");
            TrustManagerFactory factory = TrustManagerFactory.getInstance("SunX509");
            ks.load(is, keyStorePass.toCharArray());
            factory.init(ks);
            return factory.getTrustManagers();
        } catch (Exception ex) {
            Logger.error("create TrustManager failed, ex：{}", ex.getMessage());
            throw ex;
        }
    }


}
