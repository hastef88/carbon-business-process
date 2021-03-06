/**
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.core.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.exception.BPMNException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  This class loads the bean configuration from activiti.xml file
 *
 */
public class BPMNActivitiConfiguration {

    private static final Log log = LogFactory.getLog(BPMNActivitiConfiguration.class);

    private volatile static BPMNActivitiConfiguration INSTANCE = null;
    private Map<String, BPMNBean> bpmnBeanMap = null;

    private BPMNActivitiConfiguration() throws BPMNException {
        bpmnBeanMap = new HashMap<>();
        initializeBPMNConfigBeans();
    }

    public static BPMNActivitiConfiguration getInstance() {

        if(INSTANCE == null){
            synchronized (BPMNActivitiConfiguration.class){
                if(INSTANCE == null){
                    try {
                        INSTANCE = new BPMNActivitiConfiguration();
                    } catch (Exception ex){
                        log.error("Initialization of BPMNActivitiConfiguration failed. Default values will be used",
                                ex);
                    }
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Returns the BPMNBean object bean defined in activiti.xml file
     *
     * @param         beanID bean id to be fetched
     * @return        BPMNBean object corresponding to that bean in activiti.xml file
     */
    public BPMNBean getBPMNBean(String beanID){
        BPMNBean bpmnBean = bpmnBeanMap.get(beanID);
        if(bpmnBean != null){
            return bpmnBean;
        }
        return null;
    }

    /**
     * Returns the property value of a given bean
     * @param beanID         bean id
     * @param propertyName   property name to be fetched
     * @return               property value
     */
    public String getBPMNPropertyValue(String beanID, String propertyName){
        BPMNBean bpmnBean = bpmnBeanMap.get(beanID);
        if(bpmnBean != null){
            return bpmnBean.getPropertyValue(propertyName);
        }
        return null;
    }

    /**
     * Initializes the activiti.xml config file loading
     * @throws BPMNException
     */

    private void initializeBPMNConfigBeans() throws BPMNException {

        String activitiConfigPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;

        File configFile = new File(activitiConfigPath);
        try {
            String configContent = FileUtils.readFileToString(configFile);
            OMElement configElement = AXIOMUtil.stringToOM(configContent);
            Iterator beans = configElement.getChildrenWithName(new QName(BPMNConstants.SPRING_NAMESPACE,
                    BPMNConstants.BEAN));
            while (beans.hasNext()) {
                OMElement bean = (OMElement) beans.next();
                BPMNBean bpmnBean = new BPMNBean(bean);
                bpmnBeanMap.put(bpmnBean.getBeanId(), bpmnBean);
            }
        } catch (IOException e) {
            String errMsg = "Error on reading activiti configuration file ";
            throw new BPMNException(errMsg,e);
        } catch (XMLStreamException e) {
            String errMsg = "Malformed XML Error occured while processing activiti configuration file ";
            throw new BPMNException(errMsg,e);
        }
    }
}
