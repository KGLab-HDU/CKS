package code.log.appender;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Priority;

/**
 * 只输出单一的日志级别
 */
public class SingleAppender extends FileAppender {
    @Override
    public boolean isAsSevereAsThreshold(Priority priority) {
        return this.getThreshold().equals(priority);
    }
}
