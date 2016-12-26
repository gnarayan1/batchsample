package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
public class MyChunkListener implements ChunkListener {
	
	private static final Logger log = LoggerFactory.getLogger(MyChunkListener.class);

	@Override
	public void afterChunk(ChunkContext arg0) {
		log.info("Processed Chunk...");

	}

	@Override
	public void afterChunkError(ChunkContext arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeChunk(ChunkContext arg0) {
		// TODO Auto-generated method stub

	}

}
