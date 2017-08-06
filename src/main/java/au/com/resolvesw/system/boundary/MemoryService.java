package au.com.resolvesw.system.boundary;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author sfcoy
 */
@Stateless
@Path("/memory")
public class MemoryService {

    private MemoryMXBean memoryMXBean;

    @PostConstruct
    void initialise() {
        memoryMXBean = ManagementFactory.getMemoryMXBean();
    }

    @GET
    @Path("/heapMemoryUsage")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getHeapMemoryUsage() {
        final MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        return Json.createObjectBuilder()
                .add("init", heapMemoryUsage.getInit())
                .add("used", heapMemoryUsage.getUsed())
                .add("committed", heapMemoryUsage.getCommitted())
                .add("max", heapMemoryUsage.getMax())
                .build();
    }
    
    @GET
    @Path("nonHeapMemoryUsage")
    @Produces(MediaType.APPLICATION_JSON)
    public MemoryUsageBean getNonHeapMemoryUsage() {
        return new MemoryUsageBean(memoryMXBean.getNonHeapMemoryUsage());
    }

    @XmlRootElement
    public static class MemoryUsageBean {

        private final MemoryUsage memoryUsage;

        MemoryUsageBean(MemoryUsage memoryUsage) {
            this.memoryUsage = memoryUsage;
        }

        @XmlElement
        public long getInit() {
            return memoryUsage.getInit();
        }

        @XmlElement
        public long getUsed() {
            return memoryUsage.getUsed();
        }

        @XmlElement
        public long getCommitted() {
            return memoryUsage.getCommitted();
        }

        @XmlElement
        public long getMax() {
            return memoryUsage.getMax();
        }
    }
}
