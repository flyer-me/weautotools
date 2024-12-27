using CloudWork.Model;
using CloudWork.Repository.Base;
using CloudWork.Service.Interface;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudWork.Service
{
    public class QuestionService : BaseService<Question>, IQuestionService
    {
        private readonly IBaseRepository<Question> _repository;
        public QuestionService(IBaseRepository<Question> repository) : base(repository)
        {
            _repository = repository;
        }
    }
}
