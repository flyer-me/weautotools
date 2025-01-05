using CloudWork.Model;

namespace CloudWork.Service.Interface
{
    public interface ITestCaseService
    {
        Task<IEnumerable<TestCase>> GetTestCasesWithQuestionAsync();
        Task<TestCase?> GetTestCaseWithQuestionAsync(int id);
        Task<IEnumerable<Question>> GetQuestionsAsync();
        Task<Question?> GetQuestionAsync(int id);

        /// <summary>
        /// id 获取实体
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        Task<TestCase?> GetByIdAsync(int id);

        /// <summary>
        /// 获取所有实体
        /// </summary>
        /// <returns>实体枚举</returns>
        Task<IEnumerable<TestCase>> GetAllAsync();
        /// <summary>
        /// 添加实体
        /// </summary>
        /// <param name="entity"></param>
        /// <returns>Task 影响行数</returns>
        Task AddAsync(TestCase entity);
        /// <summary>
        /// 更新实体
        /// </summary>
        /// <param name="entity"></param>
        /// <returns>Task 影响行数</returns>
        void Update(TestCase entity);
        /// <summary>
        /// 删除实体
        /// </summary>
        /// <param name="id"></param>
        /// <returns>Task 影响行数</returns>
        Task DeleteAsync(int id);
        /// <summary>
        /// 保存到数据库
        /// </summary>
        /// <returns></returns>
        Task<int> SaveAsync();
    }
}
