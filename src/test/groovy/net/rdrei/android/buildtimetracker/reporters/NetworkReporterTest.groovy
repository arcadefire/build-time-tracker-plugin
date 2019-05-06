package net.rdrei.android.buildtimetracker.reporters

import groovy.mock.interceptor.MockFor
import net.rdrei.android.buildtimetracker.Timing
import org.gradle.api.logging.Logger
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class NetworkReporterTest {

    @Test
    void writeJsonToOutStream() {
        def mockLogger = new MockFor(Logger)
        mockLogger.demand.lifecycle(1) {}

        def testHttpClient = new TestHttpClient()
        NetworkReporter reporter = new NetworkReporter([url: "www.some-url.com"], mockLogger.proxyInstance(), testHttpClient)

        reporter.run([
                new Timing(100, "task1", true, false, true),
                new Timing(200, "task2", false, true, false)
        ])

        def expectedData = [
                success     : false,
                count       : 2,
                measurements: [
                        [
                                order   : 0,
                                task    : "task1",
                                success : true,
                                did_work: false,
                                skipped : true,
                                ms      : 100
                        ],
                        [
                                order   : 1,
                                task    : "task2",
                                success : false,
                                did_work: true,
                                skipped : false,
                                ms      : 200
                        ]
                ],
        ]

        assert testHttpClient.data == expectedData
    }

    @Test
    void testThrowsErrorWhenWithNoUrl() {
        def mockLogger = new MockFor(Logger)
        NetworkReporter reporter = new NetworkReporter([:], mockLogger.proxyInstance())

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
