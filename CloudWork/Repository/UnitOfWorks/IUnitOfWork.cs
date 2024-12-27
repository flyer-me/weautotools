using CloudWork.Repository.Base;

namespace CloudWork.Repository.UnitOfWorks
{
    public interface IUnitOfWork : IDisposable
    {
        IGenericRepository<T> Repository<T>() where T : class;
        TestCaseRepository TestCases { get; }

        void BeginTransaction();
        void CommitTransaction();
        void RollbackTransaction();
        Task<int> SaveAsync();
    }
}
