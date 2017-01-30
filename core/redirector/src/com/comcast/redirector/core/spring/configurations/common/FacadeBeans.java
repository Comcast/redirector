package com.comcast.redirector.core.spring.configurations.common;

import com.comcast.redirector.core.spring.configurations.base.AbstractFacadeBeans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonBeans.class})
public class FacadeBeans extends AbstractFacadeBeans {
}
