# Use the keyfactor/ejbca-ce image as the base
FROM keyfactor/ejbca-ce:latest

# Define a variable for the base modules path
ARG MODULES_PATH=/opt/keyfactor/appserver/modules/system/layers/base

# Create necessary directories
RUN mkdir -p ${MODULES_PATH}/com/qrypt/randomprovider/main \
    ${MODULES_PATH}/com/google/code/gson/main

# Copy module.xml and random-provider jar to the randomprovider module directory
COPY random-provider-1.0-SNAPSHOT.jar ${MODULES_PATH}/com/qrypt/randomprovider/main/
COPY module.xml ${MODULES_PATH}/com/qrypt/randomprovider/main/

# Copy missing google gson module dependency
COPY gson-2.8.9.jar ${MODULES_PATH}/com/google/code/gson/main
COPY module-gson.xml ${MODULES_PATH}/com/google/code/gson/main/module.xml

# Define com.qrypt.randomprovider as global-module
RUN sed -i '/<subsystem xmlns="urn:jboss:domain:ee:6\.0">/a\    <global-modules><module name="com.qrypt.randomprovider" slot="main"/></global-modules>' /opt/keyfactor/appserver/standalone/configuration/standalone.xml

# Copy random provider initializer and secure random demo to deployments dir
COPY random-provider-initializer.war /opt/keyfactor/appserver/standalone/deployments
# COPY SecureRandomDemo2.war /opt/keyfactor/appserver/standalone/deployments