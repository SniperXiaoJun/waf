package info.yangguo.waf.request.security;

import com.codahale.metrics.Timer;
import info.yangguo.waf.Constant;
import info.yangguo.waf.WafHttpHeaderNames;
import info.yangguo.waf.model.SecurityConfigIterm;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author:杨果
 * @date:2017/4/11 下午2:06
 * <p>
 * Description:
 * <p>
 * IP白名单拦截
 */
public class WIpSecurity extends Security {
    private static final Logger logger = LoggerFactory.getLogger(WIpSecurity.class);

    @Override
    public boolean isBlacklist() {
        return false;
    }

    @Override
    public boolean doFilter(HttpRequest originalRequest, HttpObject httpObject, List<SecurityConfigIterm> iterms) {
        if (httpObject instanceof HttpRequest) {
            logger.debug("filter:{}", this.getClass().getName());
            HttpRequest httpRequest = (HttpRequest) httpObject;
            for (SecurityConfigIterm iterm : iterms) {
                if (iterm.getConfig().getIsStart()) {
                    Timer itermTimer = Constant.metrics.timer("WIpSecurity[" + iterm.getName() + "]");
                    Timer.Context itermContext = itermTimer.time();
                    try {
                        Pattern pattern = Pattern.compile(iterm.getName());
                        Matcher matcher = pattern.matcher(originalRequest.headers().getAsString(WafHttpHeaderNames.X_REAL_IP));
                        if (matcher.find()) {
                            hackLog(logger, originalRequest.headers().getAsString(WafHttpHeaderNames.X_REAL_IP), "WIp", iterm.getName());
                            return true;
                        }
                    } finally {
                        itermContext.stop();
                    }
                }
            }
        }
        return false;
    }
}
