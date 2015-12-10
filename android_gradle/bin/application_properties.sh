#!/bin/bash
# ======================================================================
# Application Specific Properties (defines)
# ======================================================================

# Android Application name
application_name='RandomQuotesApp'

# Android app package name
app_package_name='com.cat.randomquotesapp'

lib_package_name='com.cat.randomquoteslib'

# android application package classes.
package_classes[0]="$app_package_name.RandomQuotesApp"
package_classes[1]="$app_package_name.AppConfiguration"

# android library package classes.
package_classes[2]="$lib_package_name.PreferencesStorage"
package_classes[3]="$lib_package_name.FilesProcessor"

# Application release mode
release_mode='debug'