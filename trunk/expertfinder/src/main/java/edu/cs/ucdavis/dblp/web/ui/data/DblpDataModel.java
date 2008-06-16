package edu.cs.ucdavis.dblp.web.ui.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

import edu.ucdavis.cs.dblp.data.DblpPubDao;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.experts.DblpResults;
import edu.ucdavis.cs.dblp.experts.SearchService;

public class DblpDataModel extends SerializableDataModel {
	private static final Logger logger = Logger.getLogger(DblpDataModel.class);

	private String currentPk;
	private boolean detached;
	private List<String> wrappedKeys;
	private Map<String, Publication> wrappedData = Maps.newHashMap();
	
	private SearchService searchService;
	private DblpResults resultsContext;
	private DblpPubDao pubDao;
	
	/**
	 * @param searchService
	 * @param resultsContext
	 * @param pubDao
	 */
	public DblpDataModel(SearchService searchService,
			DblpResults resultsContext, DblpPubDao pubDao) {
		super();
		this.searchService = searchService;
		this.resultsContext = resultsContext;
		this.pubDao = pubDao;
	}

	@Override
	public void update() {
		// not currently implemented
		logger.error("update called, but no action taken");
	}

	@Override
	public Object getRowKey() {
		return currentPk;
	}

	@Override
	public void setRowKey(Object key) {
		this.currentPk = (String)key;
	}

	@Override
	public void walk(FacesContext context, DataVisitor visitor, Range range,
			Object argument) throws IOException {
		int firstRow = ((SequenceRange)range).getFirstRow();
        int numberOfRows = ((SequenceRange)range).getRows();
        if (detached) { // Is this serialized model
// Here we just ignore current Range and use whatever data was saved in serialized model. 
// Such approach uses much more getByPk() operations, instead of just one request by range.
// Concrete case may be different from that, so you can just load data from data provider by range.
// We are using wrappedKeys list only to preserve actual order of items.
            for (String key : wrappedKeys) {
                setRowKey(key);
                visitor.process(context, key, argument);
            }
        } else { // if not serialized, than we request data from data provider
            wrappedKeys = new ArrayList<String>();
            resultsContext = searchService.fetchResultsByRange(
            								resultsContext, firstRow, numberOfRows);
            for (Publication pub : resultsContext.getPubs()) {
                wrappedKeys.add(pub.getKey());
                wrappedData.put(pub.getKey(), pub);
                visitor.process(context, pub.getKey(), argument);
            }
        }
	}

    /**
     * This method must return actual data rows count from the Data Provider. It is used by pagination control
     * to determine total number of data items.
     */
    @Override
    public int getRowCount() {
    	return (int)resultsContext.getResultsCount();
    }

	@Override
	public Object getRowData() {
		if (currentPk==null) {
            return null;
        } else {
            Publication ret = wrappedData.get(currentPk);
            if (ret==null) {
                ret = pubDao.findById(currentPk);
                wrappedData.put(currentPk, ret);
                return ret;
            } else {
                return ret;
            }
        }
	}

	@Override
	public int getRowIndex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getWrappedData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRowAvailable() {
		if (currentPk==null) {
            return false;
        } else {
            return (pubDao.findById(currentPk) != null);
        }
	}

	@Override
	public void setRowIndex(int rowIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWrappedData(Object arg0) {
		throw new UnsupportedOperationException();
	}
	
	/**
     * This method suppose to produce SerializableDataModel that will be serialized into View State and used on a post-back.
     * In current implementation we just mark current model as serialized. In more complicated cases we may need to 
     * transform data to actually serialized form.
     */
    public  SerializableDataModel getSerializableModel(Range range) {
        if (wrappedKeys!=null) {
            detached = true;
// Some activity to detach persistent data from wrappedData map may be taken here.
// In that specific case we are doing nothing.
            return this; 
        } else {
            return null;
        }
    }
    
    public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
    
    public void setPubDao(DblpPubDao pubDao) {
		this.pubDao = pubDao;
	}
    
    public void setResultsContext(DblpResults resultsContext) {
		this.resultsContext = resultsContext;
	}
}