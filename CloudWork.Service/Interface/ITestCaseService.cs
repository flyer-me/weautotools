using CloudWork.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudWork.Service.Interface
{
    public interface ITestCaseService : IBaseService<TestCase>
    {
        Task<IEnumerable<TestCase>> GetTestCasesWithQuestionAsync();
        Task<TestCase?> GetTestCaseWithQuestionAsync(int id);
    }
}
