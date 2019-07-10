package net.rdrei.android.buildtimetracker.reporters

import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import net.rdrei.android.buildtimetracker.Timing
import org.gradle.api.logging.Logger
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class NetworkReporterTest {

    final CPU_ID = "Batman i9 PPC 5Ghz TurboPascal"
    final OS = "iOS?!"
    final CURRENT_DATE = "2019-07-04 07:49:23"

    @Test
    void writeJsonToOutStream() {
        def mockLogger = new MockFor(Logger)
        mockLogger.demand.lifecycle(2) {}

        def mockSysInfo = new StubFor(SysInfo)
        mockSysInfo.demand.getCPUIdentifier(2..2) { CPU_ID }
        mockSysInfo.demand.getMaxMemory(2..2) { 1 }
        mockSysInfo.demand.getOSIdentifier(2..2) { OS }

        def mockTimeProvider = new MockFor(TrueTimeProvider)
        mockTimeProvider.demand.getCurrentDate(2..2) { CURRENT_DATE }

        def testHttpClient = new TestHttpClient()

        mockTimeProvider.use {
            mockSysInfo.use {
                NetworkReporter reporter = new NetworkReporter(
                        [url: "www.some-url.com", is_jenkins_job: true],
                        mockLogger.proxyInstance(),
                        testHttpClient,
                        "1.2.3"
                )
                reporter.plugInVersion = 2

                reporter.run([
                        new Timing(100, "task1", true, false, true),
                        new Timing(200, "task2", false, true, false)
                ])

                def expectedData = [
                        success       : false,
                        count         : 2,
                        version       : 2,
                        is_jenkins_job: true,
                        measurements  : [
                                [
                                        order   : 0,
                                        task    : "task1",
                                        success : true,
                                        did_work: false,
                                        skipped : true,
                                        ms      : 100,
                                        date    : CURRENT_DATE,
                                        cpu     : CPU_ID,
                                        memory  : 1,
                                        os      : OS

                                ],
                                [
                                        order   : 1,
                                        task    : "task2",
                                        success : false,
                                        did_work: true,
                                        skipped : false,
                                        ms      : 200,
                                        date    : CURRENT_DATE,
                                        cpu     : CPU_ID,
                                        memory  : 1,
                                        os      : OS
                                ]
                        ],
                ]

                assert testHttpClient.data == expectedData
            }
        }
    }

    @Test
    void testThrowsErrorWhenWithNoUrl() {
        def mockLogger = new MockFor(Logger)
        NetworkReporter reporter = new NetworkReporter([:], mockLogger.proxyInstance(), "1.2.3")

        def error = null
        try {
            reporter.run([])
        } catch (ReporterConfigurationError e) {
            error = e
        }

        assertNotNull error
        assertEquals ReporterConfigurationError.ErrorType.REQUIRED, error.errorType
        assertEquals "url", error.optionName
    }
}

class TestHttpClient implements HttpClient {

    Map<String, Object> data = new HashMap<String, Object>()

    @Override
    def openConnection(String urlString) {}

    @Override
    def closeConnection() {}

    @Override
    def send(Map<String, Object> data) {
        this.data = data
    }

    @Override
    String read() {
        return ""
    }
}
