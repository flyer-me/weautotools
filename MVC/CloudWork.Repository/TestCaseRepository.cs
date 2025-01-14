using CloudWork.Model;
using CloudWork.Repository.Base;
using Microsoft.EntityFrameworkCore;

namespace CloudWork.Repository
{
    public class TestCaseRepository : BaseRepository<TestCase>, ITestCaseRepository
    {
        public TestCaseRepository(AppDbContext context) : base(context) { }

        public async Task<IEnumerable<TestCase>> GetAllTestCasesAsync()
        {
            return await DbContext.TestCases.Include(q => q.Question).ToListAsync();
        }

        public async Task<TestCase?> GetTestCaseByIdAsync(int id)
        {
            var testCase = await DbContext.TestCases
                .Include(q => q.Question)
                .FirstOrDefaultAsync(q => q.Id == id);
            return testCase;
        }
    }
}
