package com.gad.first;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.gad.first.DemosendApplication.PocSendChannel.*;

@SpringBootApplication
@EnableBinding(DemosendApplication.PocSendChannel.class)
@IntegrationComponentScan
@Configuration
public class DemosendApplication {


    public static void main( String[] args ) {
        SpringApplication.run( DemosendApplication.class, args );


    }


    @MessagingGateway
    public interface PocGateway {

        @Gateway(requestChannel = GAD_PRODUCE)
        void initiateSend( PocInfo pocInfo, @Header("GAD") String ses );

    }

    public interface PocSendChannel {
        String GAD_PRODUCE = "pocGADProducer";

        @Output
        MessageChannel pocGADProducer();
    }

    @RestController()
    public class PocSendController {

        private final Logger log = LoggerFactory.getLogger( PocSendController.class );
        @Autowired
        PocGateway pocGateway;


        @GetMapping("/send/{it}")
        public void sendPocMsg( @PathVariable(value = "it") String it) {
            log.info("********* in");
            for ( int index = 0; index < Integer.valueOf( it ); index++ ) {
                PocInfo pocInfo = new PocInfo( "gad", UUID.randomUUID().toString() );

                log.info("*** send message with body: [{}]", pocInfo);
                pocGateway.initiateSend( pocInfo, "gad" + index );

            }
        }

    }



}
