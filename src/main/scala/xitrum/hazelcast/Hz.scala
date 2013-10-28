package xitrum.hazelcast

object Hz {
  private[this] val HAZELCAST_MODE_CLUSTER_MEMBER = "clusterMember"
  private[this] val HAZELCAST_MODE_JAVA_CLIENT    = "javaClient"

  val hazelcastMode = config.getString("hazelcastMode")


  /**
   * Use lazy to avoid starting Hazelcast if it is not used.
   * Starting Hazelcast takes several seconds, sometimes we want to work in
   * sbt console mode and don't like this overhead.
   */
  lazy val hazelcastInstance: HazelcastInstance = {
    // http://www.hazelcast.com/docs/2.6/manual/multi_html/ch12s07.html
    System.setProperty("hazelcast.logging.type", "slf4j")

    if (xitrum.hazelcastMode == HAZELCAST_MODE_CLUSTER_MEMBER) {
      val path = Config.root + File.separator + "config" + File.separator + "hazelcast_cluster_member.xml"
      System.setProperty("hazelcast.config", path)

      // null: load from "hazelcast.config" system property above
      // http://www.hazelcast.com/docs/2.6/manual/multi_html/ch12.html
      Hazelcast.newHazelcastInstance(null)
    } else {
      // https://github.com/hazelcast/hazelcast/issues/93
      val clientConfig = new ClientConfigBuilder("hazelcast_java_client.properties").build()
      HazelcastClient.newHazelcastClient(clientConfig)
    }
  }
}


/**
 * Shutdowns Hazelcast and calls System.exit(-1).
 * Once Hazelcast is started, calling only System.exit(-1) does not stop
 * the current process!
 */
if (xitrum.cache != null) xitrum.cache.stop()

