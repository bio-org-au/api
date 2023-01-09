package au.org.biodiversity.nslapi

import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClientConfiguration
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.ApplicationConfiguration
import jakarta.inject.Singleton

import java.time.Duration

@Singleton
@Client("httpClient")
class TestHttpClientConfiguration extends HttpClientConfiguration {
    TestHttpClientConfiguration(ApplicationConfiguration applicationConfiguration) {
        super(applicationConfiguration)
        setFollowRedirects(false)
        setReadTimeout(Duration.ofSeconds(40))
    }

    @Override
    ConnectionPoolConfiguration getConnectionPoolConfiguration() {
        ConnectionPoolConfiguration poolConfiguration = new DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration()
        return poolConfiguration
    }
}
