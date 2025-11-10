package nz.ac.canterbury.seng302.homehelper;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcImagesConfig implements WebMvcConfigurer {

    /**
     * ChatGPT code, Sets up profile images as a way to access uploaded images from the user for user profiles.
     * @param registry Stores registrations of resource handles for serving static resources
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profileImages/**")
                .addResourceLocations("file:uploads/images/");
    }
}
