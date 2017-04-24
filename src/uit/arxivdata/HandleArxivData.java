package uit.arxivdata;

public class HandleArxivData {
	public static void main(String[] args) {
		Utils u = new Utils();
		u.identidyEgdesAndVertices(
				"E:/UIT-MASTER/SOCIAL_NETWORK/KEYPLAYERS_SPARK/data/arxiv/data_test.xml",
				"E:/UIT-MASTER/SOCIAL_NETWORK/KEYPLAYERS_SPARK/HandleArxivData/output/output_datatest.json");
	}
}
