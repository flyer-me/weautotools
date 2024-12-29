using CloudWork.Model;

namespace CloudWork.Service.Interface
{
    public interface ITestCaseService : IBaseService<TestCase>
    {
        Task<IEnumerable<TestCase>> GetTestCasesWithQuestionAsync();
        Task<TestCase?> GetTestCaseWithQuestionAsync(int id);
    }
}
