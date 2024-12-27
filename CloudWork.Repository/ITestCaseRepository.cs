using CloudWork.Model;
using CloudWork.Repository.Base;

namespace CloudWork.Repository
{
    public interface ITestCaseRepository : IBaseRepository<TestCase>
    {
        // 以下完成附带导航属性的查询

        /// <summary>获取所有TestCase附带导航属性</summary>
        /// <returns>Task IEnumerable TestCase</returns>
        Task<IEnumerable<TestCase>> GetAllTestCasesAsync();

        /// <summary>根据Id获取TestCase附带导航属性</summary>
        /// <returns>Task TestCase?</returns>
        Task<TestCase?> GetTestCaseByIdAsync(int id);
    }
}
