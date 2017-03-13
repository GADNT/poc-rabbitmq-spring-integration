package com.gad.second;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;


@SpringBootApplication
@EnableBinding(DemoreceiveApplication.PocConsumeChannel.class)
@IntegrationComponentScan
@Configuration
public class DemoreceiveApplication {

    public static void main( String[] args ) {
        SpringApplication.run( DemoreceiveApplication.class, args );
    }


    @Service
    public class MyHandler {
        private Logger log = LoggerFactory.getLogger( MyHandler.class );
        @Autowired
        WsProcessor wsProcessor;

        @StreamListener(PocConsumeChannel.CONSUMER)
        public void processFromGateway( PocInfo workUnit, @Header("GAD") String wsSessionId )
                throws InterruptedException, IOException {

            log.info( "*** Handling " +
                    workUnit.getId() + ";=" + workUnit.getName() );


            wsProcessor.processDataUpdates( workUnit, wsSessionId );
        }


    }

    @MessageEndpoint
    @Import(SpelExpressionConverterConfiguration.class)
    private class WsProcessor {
        private Logger log = LoggerFactory.getLogger( WsProcessor.class );

        @Filter(inputChannel = PocConsumeChannel.CONSUMER)
        public boolean filter( Message<PocInfo> message ) {
            log.info( "***Filter called for message = {}", message );
            boolean canDeliverMessageToWsSessionOwner = true;

            try {
                PocInfo pocInfo = message.getPayload();

                String websocketSessionId = (String) message.getHeaders().get( "GAD" );
//canDeliverMessageToWsSessionOwner = websocketSessionId.equals( "gad0" );
                log.info( "Filter called and the sessid={} ; poc={}" , websocketSessionId, pocInfo );
            } catch ( Exception e ) {
                log.info( "Error when trying to filter message: " + e );
            }

            return canDeliverMessageToWsSessionOwner;
        }

        public void processDataUpdates( PocInfo workUnit, String wsSessionId ) {
            if ( workUnit != null && wsSessionId != null ) {
                log.info( "work={},;session={} " ,workUnit, wsSessionId );
            }
        }
    }

    public interface PocConsumeChannel {
        String CONSUMER = "pocGADConsumer";

        @Input
        SubscribableChannel pocGADConsumer();

    }


}
