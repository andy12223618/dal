package test.com.ctrip.platform.dal.dao.unittests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ctrip.platform.dal.dao.DalClient;
import com.ctrip.platform.dal.dao.DalClientFactory;
import com.ctrip.platform.dal.dao.DalHintEnum;
import com.ctrip.platform.dal.dao.DalHints;
import com.ctrip.platform.dal.dao.DalParser;
import com.ctrip.platform.dal.dao.DalTableDao;
import com.ctrip.platform.dal.dao.KeyHolder;
import com.ctrip.platform.dal.dao.StatementParameters;
import com.ctrip.platform.dal.dao.helper.AbstractDalParser;

public class DalTabelDaoMySqlTest {
	private final static String DATABASE_NAME = "dao_test";
	private final static String TABLE_NAME = "dal_client_test";
	private final static String DROP_TABLE_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
	private final static String CREATE_TABLE_SQL = "CREATE TABLE dal_client_test("
			+ "id int UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT, "
			+ "quantity int,"
			+ "type smallint, "
			+ "address VARCHAR(64) not null, "
			+ "last_changed timestamp default CURRENT_TIMESTAMP)";
	
	private static DalClient client = null;
	private static DalTableDao<ClientTestModel> dao = null;
	private static DalParser<ClientTestModel> parser = null;
	static {
		try {
			DalClientFactory.initClientFactory();
			client = DalClientFactory.getClient(DATABASE_NAME);
			parser = new ClientTestDalParser();
			dao = new DalTableDao<ClientTestModel>(parser);
//			dao.setDatabaseCategory(DatabaseCategory.MySql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DalHints hints = new DalHints();
		String[] sqls = new String[] { DROP_TABLE_SQL, CREATE_TABLE_SQL};
		client.batchUpdate(sqls, hints);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		DalHints hints = new DalHints();
		String[] sqls = new String[] { DROP_TABLE_SQL };
		client.batchUpdate(sqls, hints);
	}
	
	@Before
	public void setUp() throws Exception {
		DalHints hints = new DalHints();
		String[] insertSqls = new String[] {
				"INSERT INTO " + TABLE_NAME
						+ " VALUES(1, 10, 1, 'SH INFO', NULL)",
				"INSERT INTO " + TABLE_NAME
						+ " VALUES(2, 11, 1, 'BJ INFO', NULL)",
				"INSERT INTO " + TABLE_NAME
						+ " VALUES(3, 12, 2, 'SZ INFO', NULL)",
				"INSERT INTO " + TABLE_NAME
						+ " VALUES(4, 12, 1, 'HK INFO', NULL)"};
		client.batchUpdate(insertSqls, hints);
	}

	@After
	public void tearDown() throws Exception {
		String sql = "DELETE FROM " + TABLE_NAME;
		StatementParameters parameters = new StatementParameters();
		DalHints hints = new DalHints();
		client.update(sql, parameters, hints);
	}
	
	/**
	 * Test Query by Primary key
	 * @throws SQLException
	 */
	@Test
	public void testQueryByPk() throws SQLException {
		ClientTestModel model = dao.queryByPk(1, new DalHints());
		Assert.assertTrue(null != model);
		Assert.assertEquals(10, model.getQuantity().intValue());
	}

	/**
	 * Query by Entity with Primary key
	 * @throws SQLException
	 */
	@Test
	public void testQueryByPkWithEntity() throws SQLException{
		ClientTestModel pk = new ClientTestModel();
		pk.setId(1);
		ClientTestModel model = dao.queryByPk(pk, new DalHints());
		Assert.assertTrue(null != model);
		Assert.assertEquals(10, model.getQuantity().intValue());
	}
	
	/**
	 * Query by Entity without Primary key
	 * @throws SQLException
	 */
	@Test
	public void testQueryByPkWithEntityNoId() throws SQLException{
		ClientTestModel pk = new ClientTestModel();
		try {
			Assert.assertNull(dao.queryByPk(pk, new DalHints()));
		} catch (SQLException e) { 
			Assert.fail();
		}
	}
	
	/**
	 * Query against sample entity
	 * @throws SQLException
	 */
	@Test
	public void testQueryLike() throws SQLException{
		ClientTestModel pk = new ClientTestModel();
		pk.setType((short)1);
		List<ClientTestModel> models = dao.queryLike(pk, new DalHints());
		Assert.assertTrue(null != models);
		Assert.assertEquals(3, models.size());
	}
	
	/**
	 * Query by Entity with where clause
	 * @throws SQLException
	 */
	@Test
	public void testQueryWithWhereClause() throws SQLException{
		String whereClause = "type=? and id=?";
		StatementParameters parameters = new StatementParameters();
		parameters.set(1, Types.SMALLINT, 1);
		parameters.set(2, Types.INTEGER, 1);
		
		List<ClientTestModel> models = dao.query(whereClause, parameters, new DalHints());
		Assert.assertTrue(null != models);
		Assert.assertEquals(1, models.size());
		Assert.assertEquals("SH INFO", models.get(0).getAddress());
	}
	
	/**
	 * Test Query the first row with where clause
	 * @throws SQLException 
	 */
	@Test
	public void testQueryFirstWithWhereClause() throws SQLException{
		String whereClause = "type=?";
		StatementParameters parameters = new StatementParameters();
		parameters.set(1, Types.SMALLINT, 1);
		
		ClientTestModel model = dao.queryFirst(whereClause, parameters, new DalHints());
		Assert.assertTrue(null != model);
		Assert.assertEquals(1, model.getId().intValue());
	}
	
	/**
	 * Test Query the first row with where clause failed
	 * @throws SQLException
	 */
	@Test
	public void testQueryFirstWithWhereClauseFailed() throws SQLException{
		String whereClause = "type=?";
		StatementParameters parameters = new StatementParameters();
		parameters.set(1, Types.SMALLINT, 10);
		try{
			dao.queryFirst(whereClause, parameters, new DalHints());
			Assert.fail();
		}catch(Throwable e) {
		}
	}
	
	/**
	 * Test Query the top rows with where clause
	 * @throws SQLException
	 */
	@Test
	public void testQueryTopWithWhereClause() throws SQLException{
		String whereClause = "type=?";
		StatementParameters parameters = new StatementParameters();
		parameters.set(1, Types.SMALLINT, 1);
		
		List<ClientTestModel> models = dao.queryTop(whereClause, parameters, new DalHints(), 2);
		Assert.assertTrue(null != models);
		Assert.assertEquals(2, models.size());
	}
	
	/**
	 * Test Query the top rows with where clause failed
	 * @throws SQLException
	 */
	@Test
	public void testQueryTopWithWhereClauseFailed() throws SQLException{
		String whereClause = "type=?";
		StatementParameters parameters = new StatementParameters();
		parameters.set(1, Types.SMALLINT, 10);
		
		List<ClientTestModel> models = dao.queryTop(whereClause, parameters, new DalHints(), 2);
		Assert.assertTrue(null != models);
		Assert.assertEquals(0, models.size());
	}
	
	/**
	 * Test Query range of result with where clause
	 * @throws SQLException
	 */
	@Test
	public void testQueryFromWithWhereClause() throws SQLException{
		String whereClause = "type=?";
		StatementParameters parameters = new StatementParameters();
		parameters.set(1, Types.SMALLINT, 1);
		
		List<ClientTestModel> models = dao.queryFrom(whereClause, parameters, new DalHints(), 0, 1);
		Assert.assertTrue(null != models);
		Assert.assertEquals(1, models.size());
	}
	
	/**
	 * Test Query range of result with where clause failed when return not enough recodes
	 * @throws SQLException
	 */
	@Test
	public void testQueryFromWithWhereClauseFailed() throws SQLException{
		String whereClause = "type=?";
		StatementParameters parameters = new StatementParameters();
		parameters.set(1, Types.SMALLINT, 1);
		
		List<ClientTestModel> models = dao.queryFrom(whereClause, parameters, new DalHints(), 0, 10);
		Assert.assertTrue(null != models);
		Assert.assertEquals(3, models.size());
	}
	
	/**
	 * Test Query range of result with where clause when return empty collection
	 * @throws SQLException
	 */
	@Test
	public void testQueryFromWithWhereClauseEmpty() throws SQLException{
		String whereClause = "type=?";
		StatementParameters parameters = new StatementParameters();
		parameters.set(1, Types.SMALLINT, 10);
		
		List<ClientTestModel> models = dao.queryFrom(whereClause, parameters, new DalHints(), 0, 10);
		Assert.assertTrue(null != models);
		Assert.assertEquals(0, models.size());
	}
	
	/**
	 * Test Insert with null or empty parameters
	 * @throws SQLException
	 */
	@Test
	public void testInsertCheckForData() throws SQLException{
		int res;
		ClientTestModel model = null;
		try {
			res = dao.insert(new DalHints(), model);
			Assert.fail();
		} catch (Exception e) {
		}
		
		List<ClientTestModel> models = null;
		try {
			dao.insert(new DalHints(), models);
			Assert.fail();
		} catch (Exception e) {
		}
		

		models = new ArrayList<>();
		int[] ress;
		ress = dao.insert(new DalHints(), models);
		assertEquals(0, ress);
	}
		
	/**
	 * Test Insert multiple entities one by one
	 * @throws SQLException
	 */
	@Test
	public void testInsertSingle() throws SQLException{
		ClientTestModel model = new ClientTestModel();
		model.setQuantity(10 + 1%3);
		model.setType(((Number)(1%3)).shortValue());
		model.setAddress("CTRIP");
		int res = dao.insert(new DalHints(), model);
		Assert.assertEquals(1, res);
	}
	
	/**
	 * Test Insert multiple entities one by one
	 * @throws SQLException
	 */
	@Test
	public void testInsertMultipleAsList() throws SQLException{
		List<ClientTestModel> entities = new ArrayList<ClientTestModel>();
		for (int i = 0; i < 3; i++) {
			ClientTestModel model = new ClientTestModel();
			model.setQuantity(10 + 1%3);
			model.setType(((Number)(1%3)).shortValue());
			model.setAddress("CTRIP");
			entities.add(model);
		}
		int[] res = dao.insert(new DalHints(), entities);
		assertEquals(3, res);
	}
	
	/**
	 * Test Test Insert multiple entities one by one with continueOnError hints
	 * @throws SQLException
	 */
	@Test
	public void testInsertMultipleAsListWithContinueOnErrorHints() throws SQLException{
		List<ClientTestModel> entities = new ArrayList<ClientTestModel>();
		for (int i = 0; i < 3; i++) {
			ClientTestModel model = new ClientTestModel();
			model.setQuantity(10 + 1%3);
			model.setType(((Number)(1%3)).shortValue());
			if(i==1){
				model.setAddress("CTRIPCTRIPCTRIPCTRIPCTRIPCTRIPCTRIP"
						+ "CTRIPCTRIPCTRIPCTRIPCTRIPCTRIPCTRIPCTRIPCTRIP"
						+ "CTRIPCTRIPCTRIPCTRIP");
			}
			else{
				model.setAddress("CTRIP");
			}
			entities.add(model);
		}
		
		DalHints hints = new DalHints(DalHintEnum.continueOnError);
		int[] res = dao.insert(hints, entities);
		assertEquals(2, res);
	}
	
	/**
	 * Test Insert multiple entities with key-holder
	 * @throws SQLException
	 */
	@Test
	public void testInsertMultipleAsListWithKeyHolder() throws SQLException{
		List<ClientTestModel> entities = new ArrayList<ClientTestModel>();
		for (int i = 0; i < 3; i++) {
			ClientTestModel model = new ClientTestModel();
			model.setQuantity(10 + 1%3);
			model.setType(((Number)(1%3)).shortValue());
			model.setAddress("CTRIP");
			entities.add(model);
		}
		KeyHolder holder = new KeyHolder();
		int[] res = dao.insert(new DalHints(),holder, entities);
		assertEquals(3, res);
		Assert.assertEquals(3, holder.size());
		Assert.assertTrue(holder.getKeyList().get(0).containsKey("GENERATED_KEY"));
	}
	
	@Test
	public void testCombinedInsertCheckForData() throws SQLException{
		int res;
		
		List<ClientTestModel> modelList = null;
		try {
			res = dao.combinedInsert(new DalHints(), null, modelList);
			Assert.fail();
		} catch (Exception e) {
		}
		
		modelList = new ArrayList<>();
		res = dao.combinedInsert(new DalHints(), null, modelList);
		Assert.assertEquals(0, res);
	}

	/**
	 * Test Insert multiple entities with one SQL Statement
	 * @throws SQLException
	 */
	@Test
	public void testCombinedInsert() throws SQLException{
		List<ClientTestModel> entities = new ArrayList<ClientTestModel>();
		for (int i = 0; i < 3; i++) {
			ClientTestModel model = new ClientTestModel();
			model.setQuantity(10 + 1%3);
			model.setType(((Number)(1%3)).shortValue());
			model.setAddress("CTRIP");
			entities.add(model);
		}
		KeyHolder holder = new KeyHolder();
		DalHints hints = new DalHints();
		int res = dao.combinedInsert(hints, holder, entities);
		Assert.assertEquals(3, res);
		Assert.assertEquals(3, holder.size());
		Assert.assertTrue(holder.getKeyList().get(0).containsKey("GENERATED_KEY"));		
	}
	
	@Test
	public void testBatchInsertCheckForData() throws SQLException{
		int[] res;
		
		List<ClientTestModel> modelList = null;
		try {
			res = dao.batchInsert(new DalHints(), modelList);
			Assert.fail();
		} catch (Exception e) {
		}
		
		modelList = new ArrayList<>();
		res = dao.batchInsert(new DalHints(), modelList);
		Assert.assertArrayEquals(new int[0], res);
	}
	
	/**
	 * Test Batch Insert multiple entities
	 * @throws SQLException
	 */
	@Test
	public void testBatchInsert() throws SQLException{
		List<ClientTestModel> entities = new ArrayList<ClientTestModel>();
		for (int i = 0; i < 3; i++) {
			ClientTestModel model = new ClientTestModel();
			model.setQuantity(10 + 1%3);
			model.setType(((Number)(1%3)).shortValue());
			model.setAddress("CTRIP");
			entities.add(model);
		}
		int[] res = dao.batchInsert(new DalHints(), entities);
		Assert.assertTrue(res.length == 3);
		Assert.assertEquals(3, dao.query("id>4", new StatementParameters(), new DalHints()).size());

//		for (int i = 0; i < 3; i++){
//			Assert.assertTrue(res[i] > 0);
//		}
	}
	
	/**
	 * Test delete multiple entities
	 * @throws SQLException
	 */
	@Test
	public void testDelete() throws SQLException{
		ClientTestModel model = new ClientTestModel();
		model.setId(1);

		int res = dao.delete(new DalHints(), model);
		Assert.assertEquals(1, res);
	}
	
	@Test
	public void testBatchDeleteCheckForData() throws SQLException{
		int[] res;
		
		List<ClientTestModel> modelList = null;
		try {
			res = dao.batchDelete(new DalHints(), modelList);
			Assert.fail();
		} catch (Exception e) {
		}
		
		modelList = new ArrayList<>();
		res = dao.batchDelete(new DalHints(), modelList);
		Assert.assertArrayEquals(new int[0], res);
	}
	
	/**
	 * Test batch delete multiple entities
	 * @throws SQLException
	 */
	@Test
	public void testBatchDelete() throws SQLException{
		List<ClientTestModel> entities = new ArrayList<ClientTestModel>();
		for (int i = 0; i < 3; i++) {
			ClientTestModel model = new ClientTestModel();
			model.setId(i+1);
			entities.add(model);
		}
		int[] res = dao.batchDelete(new DalHints(), entities);
		Assert.assertArrayEquals(new int[]{1,1,1}, res);
	}
	
	@Test
	public void testUpdateCheckForData() throws SQLException{
		int[] res;

		List<ClientTestModel> modelList = null;
		try {
			res = dao.update(new DalHints(), modelList);
			Assert.fail();
		} catch (Exception e) {
		}
		
		modelList = new ArrayList<>();
		res = dao.update(new DalHints(), modelList);
		assertEquals(0, res);
	}
	
	/**
	 * Test update multiple entities with primary key
	 * @throws SQLException
	 */
	@Test
	public void testUpdateMultiple() throws SQLException{
		DalHints hints = new DalHints();
		List<ClientTestModel> models = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			ClientTestModel model = new ClientTestModel();
			model.setId(i+1);
			model.setAddress("CTRIP");
			models.add(model);
		}
		int[] res = dao.update(hints, models);
		assertEquals(3, res);
		
		ClientTestModel model = dao.queryByPk(1, hints);
		Assert.assertTrue(null != model);
		Assert.assertEquals("CTRIP", model.getAddress());
	}
	
	/**
	 * Test update multiple entities with primary key
	 * @throws SQLException
	 */
	@Test
	public void testBatchUpdate() throws SQLException{
		DalHints hints = new DalHints();
		List<ClientTestModel> modelList = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			ClientTestModel model = new ClientTestModel();
			model.setId(i+1);
			model.setAddress("CTRIP");
			modelList.add(model);
		}
		dao.batchUpdate(hints, modelList);
		Assert.assertEquals(3, DalTestHelper.getCount(dao, "address='CTRIP'"));
		
		ClientTestModel model = dao.queryByPk(1, hints);
		Assert.assertTrue(null != model);
		Assert.assertEquals("CTRIP", model.getAddress());
	}
	
	private void assertEquals(int expected, int[] res) {
		int total = 0;
		for(int t: res)total+=t;
		Assert.assertEquals(expected, total);
	}
	
	/**
	 * Test delete entities with where clause and parameters
	 * @throws SQLException
	 */
	@Test
	public void testDeleteWithWhereClause() throws SQLException{
		String whereClause = "type=?";
		StatementParameters parameters = new StatementParameters();
		parameters.set(1, Types.SMALLINT, 1);
		DalHints hints = new DalHints();
		int res = dao.delete(whereClause, parameters, hints);
		Assert.assertEquals(3, res);
		
		List<ClientTestModel> models = dao.query(whereClause, parameters, hints);
		Assert.assertEquals(0, models.size());
	}
	
	/**
	 * Test plain update with SQL
	 * @throws SQLException
	 */
	@Test
	public void testUpdatePlain() throws SQLException{
		String sql = "UPDATE " + TABLE_NAME
				+ " SET address = 'CTRIP' WHERE id = 1";
		StatementParameters parameters = new StatementParameters();
		DalHints hints = new DalHints();
		int res = dao.update(sql, parameters, hints);
		Assert.assertEquals(1, res);
		
		ClientTestModel model = dao.queryByPk(1, hints);
		Assert.assertTrue(null != model);
		Assert.assertEquals("CTRIP", model.getAddress());
	}
	
	
	private static class ClientTestDalParser extends AbstractDalParser<ClientTestModel>{
		private static final String databaseName="dao_test";
		private static final String tableName= "dal_client_test";
		private static final String[] columnNames = new String[]{
			"id","quantity","type","address","last_changed"
		};
		private static final String[] primaryKeyNames = new String[]{"id"};
		private static final int[] columnTypes = new int[]{
			Types.INTEGER, Types.INTEGER, Types.SMALLINT, Types.VARCHAR, Types.TIMESTAMP
		};
		
		public ClientTestDalParser() {
			super(databaseName, tableName, columnNames, primaryKeyNames, columnTypes);
		}

		@Override
		public ClientTestModel map(ResultSet rs, int rowNum)
				throws SQLException {
			ClientTestModel model = new ClientTestModel();
			model.setId(rs.getInt(1));
			model.setQuantity(rs.getInt(2));
			model.setType(rs.getShort(3));
			model.setAddress(rs.getString(4));
			model.setLastChanged(rs.getTimestamp(5));
			return model;
		}

		@Override
		public boolean isAutoIncrement() {
			return true;
		}

		@Override
		public Number getIdentityValue(ClientTestModel pojo) {
			return pojo.getId();
		}

		@Override
		public Map<String, ?> getPrimaryKeys(ClientTestModel pojo) {
			Map<String, Object> keys = new LinkedHashMap<String, Object>();
			keys.put("id", pojo.getId());
			return keys;
		}

		@Override
		public Map<String, ?> getFields(ClientTestModel pojo) {
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("id", pojo.getId());
			map.put("quantity", pojo.getQuantity());
			map.put("type", pojo.getType());
			map.put("address", pojo.getAddress());
			map.put("last_changed", pojo.getLastChanged());
			return map;
		}
		
	}
	
	private static class ClientTestModel {
		private Integer id;
		private Integer quantity;
		private Short type;
		private String address;
		private Timestamp lastChanged;

		public Integer getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		public Short getType() {
			return type;
		}

		public void setType(short type) {
			this.type = type;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Timestamp getLastChanged() {
			return lastChanged;
		}

		public void setLastChanged(Timestamp lastChanged) {
			this.lastChanged = lastChanged;
		}
	}
}
