package net.rdrei.android.buildtimetracker.reporters

import net.rdrei.android.buildtimetracker.Timing
import org.gradle.api.logging.Logger

class NetworkReporter extends AbstractBuildTimeTrackerReporter {

    private HttpClient httpClient

    private def isJenkinsJob

    NetworkReporter(
            Map<String, String> options,
            Logger logger,
            HttpClient httpClient,
            String plugInVersion
    ) {
        super(options, logger, plugInVersion)
        this.httpClient = httpClient
        this.isJenkinsJob = getOption("is_jenkins_job", false)
    }

    NetworkReporter(Map<String, String> options, Logger logger, String plugInVersion) {
        this(options, logger, new DefaultHttpClient(), plugInVersion)
    }

    @Override
    def run(List<Timing> timings) {
        String urlString = getOption("url", "")

        if (urlString == "") {
            throw new ReporterConfigurationError(
                    ReporterConfigurationError.ErrorType.REQUIRED,
                    this.getClass().getSimpleName(),
                    "url"
            )
        }

        def sysInfo = new SysInfo()
        def trueTimeProvider = new TrueTimeProvider()

        try {
            httpClient.openConnection(urlString)

            logger.lifecycle("BuildTimeTracker: opening connection to " + urlString)

            def measurements = []
            timings.eachWithIndex { it, index ->
                measurements << [
                        order: index,
                        task: it.path,
                        success: it.success,
                        did_work: it.didWork,
                        skipped: it.skipped,
                        ms: it.ms,
                        date: trueTimeProvider.getCurrentDate(),
                        cpu: sysInfo.getCPUIdentifier(),
                        memory: sysInfo.getMaxMemory(),
                        os: sysInfo.getOSIdentifier()
                ]
            }

            def data = [
                success: timings.every { it.success },
                count: timings.size(),
                version: plugInVersion,
                is_jenkins_job: isJenkinsJob,
                measurements: measurements
            ]

            httpClient.send(data)
            logger.lifecycle("BuildTimeTracker: Your build time stats has been sent.")
        } catch (Exception exception) {
            logger.lifecycle("There was an exception... $exception")
        } finally {
            httpClient.closeConnection()
        }
    }
}
