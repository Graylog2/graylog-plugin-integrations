package org.graylog.integrations.aws;

import com.google.common.collect.ImmutableSet;
import org.graylog2.plugin.security.Permission;
import org.graylog2.plugin.security.PluginPermissions;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.graylog2.plugin.security.Permission.create;

public class AWSPermissions implements PluginPermissions {

    public static final String INPUTS_READ = "inputs:read";
    private static final String INPUTS_EDIT = "inputs:edit";

    private final ImmutableSet<Permission> permissions = ImmutableSet.of(
            create(INPUTS_READ, "Read available AWS Inputs"),
            create(INPUTS_EDIT, "Edit available AWS Inputs")
    );

    @Override
    public Set<Permission> permissions() {
        return permissions;
    }

    @Override
    public Set<Permission> readerBasePermissions() {
        return Collections.emptySet();
    }

    public Set<String> allPermissions() {
        return new AWSPermissions().permissions().stream()
                                   .map(Permission::permission)
                                   .collect(Collectors.toSet());
    }
}
