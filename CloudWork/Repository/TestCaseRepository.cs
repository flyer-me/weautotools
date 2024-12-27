using CloudWork.Data;
using CloudWork.Models;
using CloudWork.Repository.Base;
using Microsoft.EntityFrameworkCore;

namespace CloudWork.Repository
{
    public class TestCaseRepository : GenericRepository<TestCase>, ITestCaseRepository
    {
        public TestCaseRepository(CloudWorkDbContext context) : base(context) { }

        public async Task<IEnumerable<TestCase>> GetAllTestCasesAsync()
        {
            return await _context.TestCases.Include(q => q.Question).ToListAsync();
        }

        public async Task<TestCase?> GetTestCaseByIdAsync(int id)
        {
            var testCase = await _context.TestCases
                .Include(q => q.Question)
                .FirstOrDefaultAsync(q => q.Id == id);
            return testCase;
        }
    }
}
