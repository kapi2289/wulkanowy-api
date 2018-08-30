package io.github.wulkanowy.api.register

import io.github.wulkanowy.api.BaseTest
import io.github.wulkanowy.api.grades.GradesTest
import io.github.wulkanowy.api.login.LoginTest
import io.github.wulkanowy.api.repository.LoginRepository
import io.github.wulkanowy.api.repository.RegisterRepository
import io.github.wulkanowy.api.service.LoginService
import io.github.wulkanowy.api.service.StudentAndParentService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RegisterTest : BaseTest() {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
    }

    @Test
    fun pupilsTest() {
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-uonet.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Login-success.html").readText()))
        server.enqueue(MockResponse().setBody(RegisterTest::class.java.getResource("WitrynaUczniaIRodzica.html").readText()))
        server.enqueue(MockResponse().setBody(GradesTest::class.java.getResource("OcenyWszystkie-filled.html").readText()))
        // 4x symbol
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-brak-dostepu.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-brak-dostepu.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-brak-dostepu.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-brak-dostepu.html").readText()))
        server.start(3000)

        val url = server.url("/").toString()
        val normal = RegisterRepository("default", "jan@fakelog.localhost", "jan123",
                LoginRepository("http", url.removePrefix("http://").removeSuffix("/"), "default",
                        getService(LoginService::class.java, url)),
                getService(StudentAndParentService::class.java, url))

        val res = normal.getPupils().blockingGet()

        assertEquals(1, res.size)
        assertEquals("Jan Kowal", res[0].studentName)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }
}
