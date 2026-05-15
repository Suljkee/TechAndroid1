import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HttpsURLConnection

// Bypass SSL verification for corporate proxy
val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
})
val sc = SSLContext.getInstance("SSL")
sc.init(null, trustAllCerts, SecureRandom())
HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }

pluginManagement {
    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/google/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/gradle-plugins/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/google/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
    }
}

rootProject.name = "My Application"
include(":app")
