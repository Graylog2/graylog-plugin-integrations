package org.graylog.integrations;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.graylog.integrations.inputs.paloalto.PaloAltoCodec;
import org.graylog.integrations.inputs.paloalto.PaloAltoTCPInput;
import org.graylog.integrations.notifications.ScriptAlarmCallback;
import org.graylog2.alarmcallbacks.EmailAlarmCallback;
import org.graylog2.alarmcallbacks.HTTPAlarmCallback;
import org.graylog2.plugin.PluginConfigBean;
import org.graylog2.plugin.PluginModule;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

/**
 * Extend the PluginModule abstract class here to add you plugin to the system.
 */
public class IntegrationsModule extends PluginModule {
        private static final Logger LOG = LoggerFactory.getLogger(IntegrationsModule.class);
    /**
     * Returns all configuration beans required by this plugin.
     *
     * Implementing this method is optional. The default method returns an empty {@link Set}.
     */
    @Override
    public Set<? extends PluginConfigBean> getConfigBeans() {
        return Collections.emptySet();
    }

    @Override
    protected void configure() {
        /*
         * Register your plugin types here.
         *
         * Examples:
         *
         * addMessageInput(Class<? extends MessageInput>);
         * addMessageFilter(Class<? extends MessageFilter>);
         * addMessageOutput(Class<? extends MessageOutput>);
         * addPeriodical(Class<? extends Periodical>);
         * addAlarmCallback(Class<? extends AlarmCallback>);
         * addInitializer(Class<? extends Service>);
         * addRestResource(Class<? extends PluginRestResource>);
         *
         *
         * Add all configuration beans returned by getConfigBeans():
         *
         * addConfigBeans();
         */
        // Palo Alto Networks
        LOG.debug("Registering message input: {}", PaloAltoTCPInput.NAME);
        addMessageInput(PaloAltoTCPInput.class);
        addCodec(PaloAltoCodec.NAME, PaloAltoCodec.class);

        // Script Alert Notification Callback
        Multibinder<AlarmCallback> alarmCallbackBinder = Multibinder.newSetBinder(binder(), AlarmCallback.class);
        alarmCallbackBinder.addBinding().to(ScriptAlarmCallback.class);

        TypeLiteral<Class<? extends AlarmCallback>> type = new TypeLiteral<Class<? extends AlarmCallback>>(){};
        Multibinder<Class<? extends AlarmCallback>> alarmCallbackClassBinder = Multibinder.newSetBinder(binder(), type);
        alarmCallbackClassBinder.addBinding().toInstance(ScriptAlarmCallback.class);
    }
}
