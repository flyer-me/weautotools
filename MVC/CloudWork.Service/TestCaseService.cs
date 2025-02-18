using CloudWork.Common;
using CloudWork.Model;
using CloudWork.Repository.UnitOfWork;
using CloudWork.Service.Interface;

namespace CloudWork.Service
{
    [Service]
    public class TestCaseService : ITestCaseService
    {
        private readonly IUnitOfWork _unitOfWork;
        public TestCaseService(IUnitOfWork unitOfWork)
        {
            _unitOfWork = unitOfWork;
        }

        public async Task<IEnumerable<TestCase>> GetTestCasesWithQuestionAsync()
        {
            return await _unitOfWork.TestCases.GetAllTestCasesAsync();
                //.GetAsync(t => t, w => true, null, nameof(Question));
        }

        public async Task<TestCase?> GetTestCaseWithQuestionAsync(object id)
        {
            return await _unitOfWork.TestCases.GetByIdAsync(id);
        }
        public async Task<Question?> GetQuestionAsync(object id)
        {
            return await _unitOfWork.Questions.GetByIdAsync(id);
        }

        public async Task<IEnumerable<Question>> GetQuestionsAsync()
        {
            return await _unitOfWork.Questions.GetAllAsync();
        }

        public async Task<TestCase?> GetByIdAsync(object id)
        {
            return await _unitOfWork.TestCases.GetByIdAsync(id);
        }

        public async Task<IEnumerable<TestCase>> GetAllAsync()
        {
            return await _unitOfWork.TestCases.GetAllAsync();
        }

        public async Task AddAsync(TestCase entity)
        {
            await _unitOfWork.TestCases.AddAsync(entity);
        }

        public void Update(TestCase entity)
        {
            _unitOfWork.TestCases.Update(entity);
        }

        public async Task DeleteAsync(object id)
        {
            await _unitOfWork.TestCases.DeleteAsync(id);
        }

        public async Task<int> SaveAsync()
        {
            return await _unitOfWork.SaveAsync();
        }
    }
}
