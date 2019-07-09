package net.rdrei.android.buildtimetracker.reporters

import net.rdrei.android.buildtimetracker.Timing
import org.gradle.BuildResult
import org.gradle.api.logging.Logger

abstract class AbstractBuildTimeTrackerReporter {

    Map<String, String> options
    Logger logger
    def plugInVersion

    AbstractBuildTimeTrackerReporter(Map<String, String> options, Logger logger, int plugInVersion = 0) {
        this.options = options
        this.logger = logger
        this.plugInVersion = plugInVersion
    }

    abstract run(List<Timing> timings)

    String getOption(String name, String defaultVal) {
        options[name] == null ? defaultVal : options[name]
    }

    boolean getOption(String name, boolean defaultVal) {
        options[name] == null ? defaultVal : options[name]
    }

    void onBuildResult(BuildResult result) {}
}
