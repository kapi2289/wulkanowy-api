package io.github.wulkanowy.api.login

import io.github.wulkanowy.api.BaseTest
import io.github.wulkanowy.api.auth.AccountPermissionException
import io.github.wulkanowy.api.auth.BadCredentialsException
import io.github.wulkanowy.api.register.HomepageResponse
import io.github.wulkanowy.api.repository.LoginRepository
import io.github.wulkanowy.api.service.LoginService
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class LoginTest : BaseTest() {

    private val normal by lazy {
        LoginRepository("http", "fakelog.localhost:3000", "default",
                getService(LoginService::class.java, "http://fakelog.localhost:3000/"))
    }

    private val adfs by lazy {
        LoginRepository("http", "fakelog.localhost:3001", "default",
                getService(LoginService::class.java, "http://fakelog.localhost:3001/"))
    }

    @Test
    fun adfsTest() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("ADFS-form-1.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("ADFS-form-2.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-cufs.html").readText().replace("3000", "3001")))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-uonet.html").readText().replace("3000", "3001")))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Login-success.html").readText()))
        server.start(3001)

        val res = adfs.login("jan@fakelog.cf", "jan123").blockingGet()

        assertTrue(res.schools.isNotEmpty())

        server.shutdown()
    }

    @Test
    fun normalLogin() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-uonet.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Login-success.html").readText()))
        server.start(3000)

        val res = normal.login("jan@fakelog.cf", "jan123").blockingGet()

        assertTrue(res.schools.isNotEmpty())

        server.shutdown()
    }

    @Test
    fun adfsBadCredentialsException() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("ADFS-form-1.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("ADFS-form-2.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-adfs-zle-haslo.html").readText()))
        server.start(InetAddress.getByName("fakelog.localhost"), 3001)

        val res = adfs.login("jan@fakelog.cf", "jan1234")
        val observer = TestObserver<HomepageResponse>()
        res.subscribe(observer)
        observer.assertTerminated()
        observer.assertError(BadCredentialsException::class.java)

        server.shutdown()
    }

    @Test
    fun normalBadCredentialsException() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-uonet.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-normal-zle-haslo.html").readText()))
        server.start(3000)

        val res = normal.login("jan@fakelog.cf", "jan1234")
        val observer = TestObserver<HomepageResponse>()
        res.subscribe(observer)
        observer.assertTerminated()
        observer.assertError(BadCredentialsException::class.java)

        server.shutdown()
    }

    @Test
    fun accessPermissionException() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-uonet.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-brak-dostepu.html").readText()))
        server.start(3000)

        val res = normal.login("jan@fakelog.cf", "jan123")
        val observer = TestObserver<HomepageResponse>()
        res.subscribe(observer)
        observer.assertTerminated()
        observer.assertError(AccountPermissionException::class.java)

        server.shutdown()
    }
}
