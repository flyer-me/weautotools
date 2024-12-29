using CloudWork.Common;
using CloudWork.Model;
using CloudWork.Repository.Base;
using CloudWork.Service.Interface;

namespace CloudWork.Service
{
    [Service]
    public class QuestionService : BaseService<Question>, IQuestionService
    {
        private readonly IBaseRepository<Question> _repository;
        public QuestionService(IBaseRepository<Question> repository) : base(repository)
        {
            _repository = repository;
        }
    }
}
