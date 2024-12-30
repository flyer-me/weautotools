using CloudWork.Model;
using CloudWork.Repository.Base;

namespace CloudWork.Repository.UnitOfWork
{
    public interface IUnitOfWork : IDisposable
    {
        BaseRepository<Question> Questions { get; }
        BaseRepository<User> Users { get; }
        BaseRepository<Submission> Submissions { get; }
        BaseRepository<SubmissionResult> SubmissionEvaluations { get; }
        TestCaseRepository TestCases { get; }

        void Begin();
        void Commit();
        void Rollback();
        Task<int> SaveAsync();
    }
}
